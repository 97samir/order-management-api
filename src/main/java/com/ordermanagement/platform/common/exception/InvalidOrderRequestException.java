package com.ordermanagement.platform.common.exception;

public class InvalidOrderRequestException extends RuntimeException {

    // solicitud para pedidos invalidos
    public InvalidOrderRequestException(String message) {
        super(message);
    }
}