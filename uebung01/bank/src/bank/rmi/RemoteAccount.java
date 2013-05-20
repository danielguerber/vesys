package bank.rmi;

import java.rmi.Remote;

import bank.Account;

public interface RemoteAccount extends Account, Remote {}