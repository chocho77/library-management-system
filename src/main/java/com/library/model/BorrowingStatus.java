package com.library.model;

// В BorrowingStatus enum-а, добави EXTENDED
public enum BorrowingStatus {
    BORROWED,       // заета
    RETURNED,       // върната
    OVERDUE,        // просрочена
    LOST,           // изгубена
    EXTENDED        // удължен срок (НОВ)
}