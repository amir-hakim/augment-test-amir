package common.lib.controller.backend;


public abstract class Request
{
	public Runnable cancelProcedure;
	private RequestObserver requestObserver;
	private Integer requestId;
	
	public Request(Integer requestId, RequestObserver requestObserver){
		this.requestObserver = requestObserver;
		this.requestId = requestId;
	}
	
	public abstract Object run();

	public RequestObserver getRequestObserver()
  {
  	return requestObserver;
  }

	public Integer getRequestId()
  {
  	return requestId;
  }
	
	
}
