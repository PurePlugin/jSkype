package xyz.gghost.jskype.exception;

/**
 * Created by Ghost on 19/09/2015.
 */
public class FailedToLoginException extends Exception
{
	private static final long serialVersionUID = 1L;

	public FailedToLoginException(String a)
	{
		super(a);
	}
}