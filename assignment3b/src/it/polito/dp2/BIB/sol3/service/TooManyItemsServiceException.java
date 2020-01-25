package it.polito.dp2.BIB.sol3.service;

public class TooManyItemsServiceException extends Exception {

	public TooManyItemsServiceException() {
	}

	public TooManyItemsServiceException(String message) {
		super(message);
	}

	public TooManyItemsServiceException(Throwable cause) {
		super(cause);
	}

	public TooManyItemsServiceException(String message, Throwable cause) {
		super(message, cause);
	}

	public TooManyItemsServiceException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
