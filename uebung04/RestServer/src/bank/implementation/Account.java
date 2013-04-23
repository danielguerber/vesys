package bank.implementation;

import java.util.UUID;

import bank.types.AccountData;

public final class Account {
	private final String number;
	private final String owner;
	private volatile double balance;
	private volatile boolean active = true;
	private volatile int modified;

	public Account(final String owner) {
		this.owner = owner;
		this.number = UUID.randomUUID().toString();
		this.modified = 0;
	}

	public double getBalance() {
		return balance;
	}

	public String getOwner() {
		return owner;
	}

	public String getNumber() {
		return number;
	}
	
	public int getModified() {
		return modified;
	}

	public boolean isActive() {
		return active;
	}
	
	public synchronized void setActive(final boolean active) {
		this.modified++;
		this.active = active;
	}

	public synchronized void setBalance(final double amount)  {
		
		if (amount < 0) {
			throw new IllegalArgumentException("Amount can't be negative!");
		}
		
		this.modified++;
		this.balance = amount;
	}
	
	public synchronized AccountData getAccountData() {
		AccountData data = new AccountData();
		data.setNumber(number);
		data.setOwner(owner);
		data.setBalance(balance);
		data.setActive(active);
		data.setModified(modified);
		return data;
	}
}


