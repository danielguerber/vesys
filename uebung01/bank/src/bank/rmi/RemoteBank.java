package bank.rmi;

import java.io.IOException;
import java.rmi.Remote;

import bank.Bank;

public interface RemoteBank extends Bank, Remote {
	void registerUpdateHandler(RemoteUpdateHandler handler) throws IOException;
}