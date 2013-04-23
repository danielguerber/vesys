package bank.implementation;

import java.util.UUID;

import bank.InactiveException;
import bank.OverdrawException;

public final class Account implements bank.Account {
	private String number;
	private String owner;
	private double balance;
	private boolean active = true;

	Account(final String owner) {
		this.owner = owner;
		this.number = UUID.randomUUID().toString();
	}

	@Override
	public double getBalance() {
		return balance;
	}

	@Override
	public String getOwner() {
		return owner;
	}

	@Override
	public String getNumber() {
		return number;
	}

	@Override
	public boolean isActive() {
		return active;
	}
	
	void setActive(final boolean active) {
		this.active = active;
	}


	@Override
	public void deposit(final double amount) 
			throws InactiveException {
		if (!this.isActive()) {
			throw new InactiveException("Account is closed!");
		}
		
		if (amount < 0) {
			throw new IllegalArgumentException("Amount can't be negative!");
		}
		
		this.balance += amount;
	}

	@Override
	public void withdraw(final double amount) 
			throws InactiveException, OverdrawException {
		if (!this.isActive()) {
			throw new InactiveException("Account is closed!");
		}
		
		if (amount < 0) {
			throw new IllegalArgumentException("Amount can't be negative!");
		}
		
		if (amount > balance) {
			throw new OverdrawException("Not enough money on account!");
		}
		
		this.balance -= amount;
	}

}


