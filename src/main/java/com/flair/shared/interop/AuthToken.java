package com.flair.shared.interop;

import java.io.Serializable;

/*
 * Token used to uniquely identify the client and transmit basic session state
 * Validated by the server for every request
 */
public interface AuthToken extends Serializable
{
	public enum Status
	{
		VALID,							// working statex
		INVALID_SERVER_ERROR			// internal server error
	}
	
	public Status			getStatus();
	
	@Override
	public String			toString();
}
