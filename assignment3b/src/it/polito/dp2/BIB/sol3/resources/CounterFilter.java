package it.polito.dp2.BIB.sol3.resources;

import java.io.IOException;
import java.math.BigInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.ext.Provider;

import it.polito.dp2.BIB.sol3.service.util.ResourseUtils;

@Provider
public class CounterFilter implements ContainerRequestFilter{

	CounterImpl counter;
	
	public CounterFilter() {
		counter = CounterImpl.getCounter();
	}

	@Override
	public void filter(ContainerRequestContext context) throws IOException {
		if (context.getMethod().equals("GET") && ResourseUtils.isBookshelfReadRequest(context.getUriInfo().getAbsolutePath().toString())) {
			long id = ResourseUtils.resolveBookshelfId(context.getUriInfo().getAbsolutePath().toString());
			counter.increment(BigInteger.valueOf(id));
		}
	}

}
