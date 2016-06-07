package common.lib.controller.backend;

public interface RequestObserver
{
	/**
	 * This method is called by OperationExecuterQueue class only. It must not called by any Request implementor. Method
	 * is called to send event to specific request observer telling that request has been completed. Method is called in
	 * case of any exception (un-handled or business) Method not called If request is cancelled.
	 * 
	 * @param requestId
	 * @param error
	 * @param resulObject
	 */
	public void handleRequestFinished(Object requestId, Throwable error, Object resulObject);
	public void requestCanceled(Integer requestId, Throwable error);
	public void updateStatus(Integer requestId, String statusMsg);
}
