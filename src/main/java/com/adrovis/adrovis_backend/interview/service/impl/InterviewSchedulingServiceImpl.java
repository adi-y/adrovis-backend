package com.adrovis.adrovis_backend.interview.service.impl;


import com.adrovis.adrovis_backend.career.entity.Application;
import com.adrovis.adrovis_backend.career.repository.ApplicationRepository;
import com.adrovis.adrovis_backend.email.service.EmailService;
import com.adrovis.adrovis_backend.interview.dto.request.*;
import com.adrovis.adrovis_backend.interview.dto.response.AvailabilitySlotResponse;
import com.adrovis.adrovis_backend.interview.dto.response.InterviewResponse;
import com.adrovis.adrovis_backend.interview.dto.response.InterviewSummaryResponse;
import com.adrovis.adrovis_backend.interview.entity.Interview;
import com.adrovis.adrovis_backend.interview.entity.InterviewAvailabilitySlot;
import com.adrovis.adrovis_backend.interview.entity.InterviewStatus;
import com.adrovis.adrovis_backend.interview.repository.InterviewAvailabilitySlotRepository;
import com.adrovis.adrovis_backend.interview.repository.InterviewRepository;
import com.adrovis.adrovis_backend.interview.service.InterviewSchedulingService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InterviewSchedulingServiceImpl implements InterviewSchedulingService {

    private static final Set<InterviewStatus> AVAILABILITY_EDITABLE_STATUSES =
            Set.of(InterviewStatus.AWAITING_AVAILABILITY, InterviewStatus.AVAILABILITY_SUBMITTED);

    private static final Set<InterviewStatus> SCHEDULABLE_STATUSES =
            Set.of(InterviewStatus.AWAITING_AVAILABILITY, InterviewStatus.AVAILABILITY_SUBMITTED);

    private final InterviewRepository interviewRepository;
    private final InterviewAvailabilitySlotRepository slotRepository;
    private final ApplicationRepository applicationRepository;
    private final EmailService emailService;

    @Override
    @Transactional
    public void ensureInterviewAndRequestAvailability(String applicationId) {
        Application application = findApplicationOrThrow(applicationId);

        Interview interview = interviewRepository.findByApplicationId(application.getId())
                .orElseGet(() -> Interview.builder()
                        .applicationId(application.getId())
                        .status(InterviewStatus.AWAITING_AVAILABILITY)
                        .displayTimezone("Asia/Kolkata")
                        .build());

        interview.setStatus(InterviewStatus.AWAITING_AVAILABILITY);
        interview.setAvailabilityRequestedAt(OffsetDateTime.now());
        interviewRepository.save(interview);

        //emailService.sendInterviewAvailabilityRequestEmailAsync(application);
    }

    @Override
    @Transactional(readOnly = true)
    public InterviewResponse getInterview(String applicationId) {
        Application application = findApplicationOrThrow(applicationId);
        Interview interview = findInterviewOrThrow(application.getId());
        return toResponse(application, interview);
    }

    @Override
    @Transactional
    public InterviewResponse submitAvailability(String applicationId, AvailabilitySubmissionRequest request) {
        Application application = findApplicationOrThrow(applicationId);
        Interview interview = findInterviewOrThrow(application.getId());

        if (!AVAILABILITY_EDITABLE_STATUSES.contains(interview.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Availability can no longer be submitted — interview is " + interview.getStatus());
        }

        validateSlots(request);

        slotRepository.deleteByInterviewId(interview.getId());

        ZoneId zoneId = parseZoneOrThrow(request.timezone());
        List<InterviewAvailabilitySlot> slots = request.slots().stream()
                .map(s -> InterviewAvailabilitySlot.builder()
                        .interviewId(interview.getId())
                        .availableDate(s.availableDate())
                        .startTime(s.startTime())
                        .endTime(s.endTime())
                        .timezone(request.timezone())
                        .slotStartUtc(toUtc(s.availableDate(), s.startTime(), zoneId))
                        .slotEndUtc(toUtc(s.availableDate(), s.endTime(), zoneId))
                        .build())
                .toList();
        slotRepository.saveAll(slots);

        interview.setCandidateNote(request.candidateNote());
        interview.setStatus(InterviewStatus.AVAILABILITY_SUBMITTED);
        interview.setAvailabilitySubmittedAt(OffsetDateTime.now());
        interviewRepository.save(interview);

        return toResponse(application, interview);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InterviewSummaryResponse> listInterviews(String status, Boolean pendingAvailability,
                                                         Boolean today, Boolean upcoming,
                                                         LocalDate from, LocalDate to) {
        List<Interview> interviews;

        if (status != null) {
            interviews = interviewRepository.findByStatus(InterviewStatus.valueOf(status.toUpperCase()));
        } else if (Boolean.TRUE.equals(pendingAvailability)) {
            interviews = interviewRepository.findByStatus(InterviewStatus.AWAITING_AVAILABILITY);
            interviews.addAll(interviewRepository.findByStatus(InterviewStatus.AVAILABILITY_SUBMITTED));
        } else if (Boolean.TRUE.equals(today)) {
            ZonedDateTime startOfDay = ZonedDateTime.now(ZoneId.of("Asia/Kolkata")).toLocalDate()
                    .atStartOfDay(ZoneId.of("Asia/Kolkata"));
            interviews = interviewRepository.findByScheduledStartUtcBetween(
                    startOfDay.toOffsetDateTime(), startOfDay.plusDays(1).toOffsetDateTime());
        } else if (Boolean.TRUE.equals(upcoming)) {
            interviews = interviewRepository.findByStatus(InterviewStatus.SCHEDULED);
        } else if (from != null && to != null) {
            interviews = interviewRepository.findByScheduledStartUtcBetween(
                    from.atStartOfDay(ZoneId.of("Asia/Kolkata")).toOffsetDateTime(),
                    to.plusDays(1).atStartOfDay(ZoneId.of("Asia/Kolkata")).toOffsetDateTime());
        } else {
            interviews = interviewRepository.findAll();
        }

        return interviews.stream().map(this::toSummary).toList();
    }

    @Override
    @Transactional
    public InterviewResponse scheduleInterview(String applicationId, ScheduleInterviewRequest request) {
        Application application = findApplicationOrThrow(applicationId);
        Interview interview = findInterviewOrThrow(application.getId());

        if (!SCHEDULABLE_STATUSES.contains(interview.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Interview is " + interview.getStatus() + " — use reschedule instead of schedule");
        }
        if (!request.scheduledEnd().isAfter(request.scheduledStart())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "scheduledEnd must be after scheduledStart");
        }
        if (request.scheduledStart().isBefore(OffsetDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "scheduledStart must be in the future");
        }

        interview.setScheduledStartUtc(request.scheduledStart());
        interview.setScheduledEndUtc(request.scheduledEnd());
        interview.setInterviewerName(request.interviewerName());
        interview.setMeetingLink(request.meetingLink());
        interview.setAdminNote(request.adminNote());
        interview.setStatus(InterviewStatus.SCHEDULED);
        interview.setScheduledAt(OffsetDateTime.now());
        interviewRepository.save(interview);

        emailService.sendInterviewScheduledEmailAsync(application, interview);

        return toResponse(application, interview);
    }

    @Override
    @Transactional
    public InterviewResponse rescheduleInterview(String applicationId, RescheduleInterviewRequest request) {
        Application application = findApplicationOrThrow(applicationId);
        Interview interview = findInterviewOrThrow(application.getId());

        if (interview.getStatus() != InterviewStatus.SCHEDULED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Only a SCHEDULED interview can be rescheduled (current status: " + interview.getStatus() + ")");
        }
        if (!request.scheduledEnd().isAfter(request.scheduledStart())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "scheduledEnd must be after scheduledStart");
        }

        interview.setScheduledStartUtc(request.scheduledStart());
        interview.setScheduledEndUtc(request.scheduledEnd());
        if (request.interviewerName() != null) interview.setInterviewerName(request.interviewerName());
        if (request.meetingLink() != null) interview.setMeetingLink(request.meetingLink());
        if (request.adminNote() != null) interview.setAdminNote(request.adminNote());
        interviewRepository.save(interview);

        emailService.sendInterviewRescheduledEmailAsync(application, interview);

        return toResponse(application, interview);
    }

    @Override
    @Transactional
    public InterviewResponse cancelInterview(String applicationId, CancelInterviewRequest request) {
        Application application = findApplicationOrThrow(applicationId);
        Interview interview = findInterviewOrThrow(application.getId());

        if (interview.getStatus() == InterviewStatus.CANCELLED || interview.getStatus() == InterviewStatus.COMPLETED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Interview is already " + interview.getStatus());
        }

        interview.setStatus(InterviewStatus.CANCELLED);
        interview.setAdminNote(request.reason());
        interview.setCancelledAt(OffsetDateTime.now());
        interviewRepository.save(interview);

        emailService.sendInterviewCancelledEmailAsync(application, interview);

        return toResponse(application, interview);
    }

    @Override
    @Transactional
    public InterviewResponse recordOutcome(String applicationId, InterviewOutcomeRequest request) {
        Application application = findApplicationOrThrow(applicationId);
        Interview interview = findInterviewOrThrow(application.getId());

        if (interview.getStatus() != InterviewStatus.SCHEDULED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Outcome can only be recorded for a SCHEDULED interview");
        }

        interview.setStatus(InterviewStatus.valueOf(request.outcome()));
        interviewRepository.save(interview);

        return toResponse(application, interview);
    }

    @Override
    @Transactional
    public InterviewResponse requestAvailabilityAgain(String applicationId) {
        Application application = findApplicationOrThrow(applicationId);
        Interview interview = findInterviewOrThrow(application.getId());

        if (interview.getStatus() == InterviewStatus.CANCELLED || interview.getStatus() == InterviewStatus.COMPLETED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Cannot request availability for an interview that is " + interview.getStatus());
        }

        interview.setStatus(InterviewStatus.AWAITING_AVAILABILITY);
        interview.setAvailabilityRequestedAt(OffsetDateTime.now());
        interviewRepository.save(interview);

        emailService.sendInterviewAvailabilityRequestEmailAsync(application);

        return toResponse(application, interview);
    }

    // ---------- helpers ----------

    private Application findApplicationOrThrow(String applicationId) {
        return applicationRepository.findByApplicationId(applicationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No application found with id " + applicationId));
    }

    private Interview findInterviewOrThrow(UUID applicationUuid) {
        return interviewRepository.findByApplicationId(applicationUuid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No interview record exists for this application yet (has it been shortlisted?)"));
    }

    private void validateSlots(AvailabilitySubmissionRequest request) {
        ZoneId zone = parseZoneOrThrow(request.timezone());
        LocalDate today = LocalDate.now(zone);
        LocalDate maxDate = today.plusDays(30);

        var seen = new java.util.HashSet<String>();
        for (var slot : request.slots()) {
            if (!slot.endTime().isAfter(slot.startTime())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "endTime must be after startTime for " + slot.availableDate());
            }
            if (slot.availableDate().isBefore(today)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "availableDate cannot be in the past: " + slot.availableDate());
            }
            if (slot.availableDate().isAfter(maxDate)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "availableDate must be within the next 30 days: " + slot.availableDate());
            }
            String key = slot.availableDate() + "|" + slot.startTime() + "|" + slot.endTime();
            if (!seen.add(key)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Duplicate slot submitted: " + key);
            }
        }
    }

    private ZoneId parseZoneOrThrow(String timezone) {
        try {
            return ZoneId.of(timezone);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid timezone: " + timezone);
        }
    }

    private OffsetDateTime toUtc(LocalDate date, java.time.LocalTime time, ZoneId zone) {
        return date.atTime(time).atZone(zone).toOffsetDateTime();
    }

    private InterviewResponse toResponse(Application application, Interview interview) {
        List<AvailabilitySlotResponse> slots =
                slotRepository.findByInterviewIdOrderByAvailableDateAscStartTimeAsc(interview.getId()).stream()
                        .map(s -> new AvailabilitySlotResponse(s.getAvailableDate(), s.getStartTime(), s.getEndTime(), s.getTimezone()))
                        .toList();

        return new InterviewResponse(
                application.getApplicationId(),
                interview.getStatus().name(),
                interview.getCandidateNote(),
                interview.getAdminNote(),
                slots,
                interview.getScheduledStartUtc(),
                interview.getScheduledEndUtc(),
                interview.getDisplayTimezone(),
                interview.getMeetingLink(),
                interview.getInterviewerName()
        );
    }

    private InterviewSummaryResponse toSummary(Interview interview) {
        Application application = applicationRepository.findById(interview.getApplicationId())
                .orElseThrow(() -> new EntityNotFoundException("Application missing for interview " + interview.getId()));
        int slotCount = slotRepository.findByInterviewIdOrderByAvailableDateAscStartTimeAsc(interview.getId()).size();

        return new InterviewSummaryResponse(
                application.getApplicationId(),
                application.getApplicantName(),
                application.getJobTitleSnapshot(),
                interview.getStatus().name(),
                slotCount,
                interview.getScheduledStartUtc()
        );
    }
}
