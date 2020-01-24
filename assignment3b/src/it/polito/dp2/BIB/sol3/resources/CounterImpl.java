package it.polito.dp2.BIB.sol3.resources;

import java.math.BigInteger;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import it.polito.dp2.BIB.sol3.db.ItemPage;
import it.polito.dp2.BIB.sol3.db.Neo4jDB;
import it.polito.dp2.BIB.sol3.service.SearchScope;
import it.polito.dp2.BIB.sol3.service.jaxb.Item;
import it.polito.dp2.BIB.sol3.service.util.ResourseUtils;

public class CounterImpl {
	
	private static CounterImpl counterInstance;
	
	private AtomicInteger counterTot;
	private ConcurrentHashMap<BigInteger, AtomicInteger> counterMap;
	
	
	private CounterImpl() {
		counterTot = new AtomicInteger(0);
		counterMap = new ConcurrentHashMap<>();
	}
	
	public synchronized static CounterImpl getCounter() {
		if (counterInstance == null)
			counterInstance = new CounterImpl();
		return counterInstance;
	}
	
	public synchronized void initCounter(BigInteger id) {
		AtomicInteger counter = new AtomicInteger(0);
		counterMap.putIfAbsent(id,  counter);
	}
	
	public synchronized void initCounter(Set<BigInteger> itemsSet) {
		for(BigInteger id : itemsSet) {
			AtomicInteger counter = new AtomicInteger(0);
			counterMap.putIfAbsent(id, counter);
		}
	}
	
	public synchronized int getCounterValue(BigInteger id) {
		AtomicInteger counter = counterMap.get(id);
		if (counter == null) return -1;
		return counter.get();
	}

	public int getCounterTotValue() {
		return counterTot.get();
	}
	
	public synchronized int increment(BigInteger id) {
		AtomicInteger counter = counterMap.get(id);
		if (counter == null) return -1;
		return counter.incrementAndGet();
	}
	
	public int incrementTot() {
		return counterTot.incrementAndGet();
	}
	
	public void deleteCounter(BigInteger id) {
		counterMap.remove(id);
	}
	

}
