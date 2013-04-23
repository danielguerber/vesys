package bank.implementation;

import java.util.concurrent.ConcurrentHashMap;

public class Bank {
	public static ConcurrentHashMap<String, Account> accounts = new ConcurrentHashMap<>();
	public static volatile int modified = 0;	    
}
