package bank.rmi;

import java.io.IOException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import bank.Bank;
import bank.BankDriver2;

public class Driver implements BankDriver2 {

	private RemoteBank bank;
	
	@Override
	public void connect(String[] args) throws IOException {
		try {
			bank = (RemoteBank)Naming.lookup(
					"rmi://localhost/bankService");
		} catch (NotBoundException e) {
			throw new IOException(e.getMessage());
		}
		
	}

	@Override
	public void disconnect() throws IOException {
		bank = null;
		
	}

	@Override
	public Bank getBank() {
		return bank;
	}

	@Override
	public void registerUpdateHandler(UpdateHandler handler) throws IOException {
		bank.registerUpdateHandler(new RemoteHandler(handler));
	}
	
	@SuppressWarnings("serial")
	static class RemoteHandler extends UnicastRemoteObject implements RemoteUpdateHandler {
		
		private UpdateHandler handler;

		public RemoteHandler(UpdateHandler handler) throws RemoteException {
			this.handler = handler;
		}
		
		@Override
		public void accountChanged(String id) throws IOException {
			handler.accountChanged(id);
		}
		
	}

}
