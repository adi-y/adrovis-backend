package com.adrovis.adrovis_backend.contact.enums;

/**
 * Represents the lifecycle status of a lead.
 */
public enum LeadStatus {

    /**
     * Newly created lead awaiting initial review.
     */
    NEW,

    /**
     * Initial contact has been made.
     */
    CONTACTED,

    /**
     * Lead has been qualified as a potential customer.
     */
    QUALIFIED,

    /**
     * Lead did not convert into a customer.
     */
    LOST,

    /**
     * Lead successfully converted into a customer.
     */
    CONVERTED

}