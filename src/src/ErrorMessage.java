package src;

public class ErrorMessage {
	private String msg, whichTextObject;
	ErrorMessage() {
		
	}
	ErrorMessage(String whichTextObject, String msg) {
		this.msg = msg;
		this.whichTextObject = whichTextObject;
	}
	public String getMsg() {
		return msg;
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}
	public String getWhichTextObject() {
		return whichTextObject;
	}
	public void setWhichTextObject(String whichTextObject) {
		this.whichTextObject = whichTextObject;
	}
	
}
