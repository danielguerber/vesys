package bank.resources;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import bank.implementation.Account;
import bank.implementation.Bank;
import bank.types.TransactionData;

import com.sun.jersey.spi.resource.Singleton;

@Path("/transfer")
@Singleton
public class Transfer {
	
	@POST
	public Response doTransfer(@Context UriInfo uri, TransactionData data) {
		Account fromAccount = Bank.accounts.get(data.getFromNumber());
		Account toAccount = Bank.accounts.get(data.getToNumber());
		
		if (fromAccount==null || toAccount==null) {
			return Response.status(Status.NOT_FOUND).build();
		} else {
			Account a;
			Account b;
			if (fromAccount.getNumber().compareTo(toAccount.getNumber())> 0) {
				a = fromAccount;
				b = toAccount;
			} else {
				b = fromAccount;
				a = toAccount;
			}
			
			synchronized (a) {
				synchronized (b) {
					if (!String.valueOf(fromAccount.getModified()).equals(data.getFromETag()) ||
							!String.valueOf(toAccount.getModified()).equals(data.getToEtag())) {
						return Response.status(Status.CONFLICT).build();
					}
					
					fromAccount.setBalance(data.getFromAmount());
					toAccount.setBalance(data.getToAmount());
					
					return Response.noContent().build();
				}
			}
		}
		
	}
}
