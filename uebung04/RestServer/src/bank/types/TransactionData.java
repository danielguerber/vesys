package bank.types;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "TransactionData")
public class TransactionData {
	private double fromAmount;
	private double toAmount;
	private String fromNumber, toNumber, fromETag, toEtag;
	public double getFromAmount() {
		return fromAmount;
	}
	public void setFromAmount(double fromAmount) {
		this.fromAmount = fromAmount;
	}
	public double getToAmount() {
		return toAmount;
	}
	public void setToAmount(double toAmount) {
		this.toAmount = toAmount;
	}
	public String getFromNumber() {
		return fromNumber;
	}
	public void setFromNumber(String fromNumber) {
		this.fromNumber = fromNumber;
	}
	public String getToNumber() {
		return toNumber;
	}
	public void setToNumber(String toNumber) {
		this.toNumber = toNumber;
	}
	public String getFromETag() {
		return fromETag;
	}
	public void setFromETag(String fromETag) {
		this.fromETag = fromETag;
	}
	public String getToEtag() {
		return toEtag;
	}
	public void setToEtag(String toEtag) {
		this.toEtag = toEtag;
	}
}