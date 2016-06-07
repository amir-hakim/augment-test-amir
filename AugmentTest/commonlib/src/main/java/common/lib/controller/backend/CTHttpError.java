package common.lib.controller.backend;

public class CTHttpError extends RuntimeException
{
	private static final long serialVersionUID = 1L;
	
	String msg = "";
	double statusCode = -1;

	/*
	 * Constructors
	 */
	public CTHttpError(String msg, double statusCode)
	{
		super(msg);
		this.msg = msg;
		this.statusCode = statusCode;
	}

	/*
	 * Setters & Getters
	 */
	public String getErrorMsg()
	{
		return msg;
	}

	public void setErrorMsg(String msg)
	{
		this.msg = msg;
	}

	public int getStatusCode()
	{
		return (int)statusCode;
	}

	public void setStatusCode(int statusCode)
	{
		this.statusCode = statusCode;
	}

}
