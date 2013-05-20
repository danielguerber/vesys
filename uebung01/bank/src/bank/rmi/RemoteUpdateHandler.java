package bank.rmi;

import java.rmi.Remote;
import bank.BankDriver2;

public interface RemoteUpdateHandler extends Remote, BankDriver2.UpdateHandler {

}
