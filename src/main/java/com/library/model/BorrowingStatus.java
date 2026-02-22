package com.library.model;

public enum BorrowingStatus {
    BORROWED,       // заета
    RETURNED,       // върната
    OVERDUE,        // просрочена
    LOST,           // изгубена
    EXTENDED        // удължен срок
}