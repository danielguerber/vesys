package bank.implementation;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import bank.InactiveException;
import bank.OverdrawException;

public class Bank implements bank.Bank {
	private static Bank instance;
	
	public static Bank getInstance() {
		if (instance==null) {
			instance = new Bank();
		}
		return instance;
	}
	
	private Map<String, Account> accounts = new HashMap<String, Account>();

	@Override
	public Set<String> getAccountNumbers() {
		Set<String> activeNumbers = new HashSet<String>();
		for (Account account : accounts.values()) {
			if (account.isActive()) {
				activeNumbers.add(account.getNumber());
			}
		}
		return activeNumbers;
	}

	@Override
	public String createAccount(String owner) {
		Account newAccount = new Account(owner);
		accounts.put(newAccount.getNumber(), newAccount);
		return newAccount.getNumber();
	}

	@Override
	public boolean closeAccount(String number) {
		Account closeAccount = accounts.get(number);
		if (closeAccount != null 
				&& closeAccount.getBalance() == 0
				&& closeAccount.isActive()) {
			closeAccount.setActive(false);
			return true;
		} else {
			return false;
		}
	}

	
	@Override
	public bank.Account getAccount(String number) {
		return (bank.Account) accounts.get(number);
	}

	/**
	 * Transfers the given amount from account a to account b.
	 * 
	 * @param a account to withdraw amount from
	 * @param b account to deposit amount
	 * @param amount value to transfer
	 * @pre amount >= 0
	 * @throws InactiveException if one of the two accounts is not active
	 * @throws OverdrawException if the amount is greater than the balance of
	 *             account a
	 * @throws IllegalArgumentException if the argument is negative
	 * @throws IOException if a remoting or communication problem occurs
	 */
	@Override
	public void transfer(bank.Account from, bank.Account to, double amount)
			throws IOException, InactiveException, OverdrawException {
		
		if (!from.isActive()) {
			throw new InactiveException("Source account is closed!");
		}
		
		if (!to.isActive()) {
			throw new InactiveException("Target account is closed!");
		}
		
		from.withdraw(amount);
		to.deposit(amount);
	}
}
