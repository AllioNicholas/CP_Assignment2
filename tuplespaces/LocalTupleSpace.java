package tuplespaces;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class LocalTupleSpace implements TupleSpace {
	Object lock = new Object();
	Map<String, List<Tuple>> tupleMap = new HashMap<String, List<Tuple>>();
	
	@Override
	public String toString() {
		final StringBuilder str = new StringBuilder();
		synchronized (lock) {
			for (final List<Tuple> tuples : tupleMap.values()) {
				for (final Tuple tuple : tuples) {
					str.append(tuple.toString() + "\n");
				}
			}
		}
		return str.toString();
	}
	
	public String[] get(String... pattern) {
		Tuple tuple = null;
		synchronized (lock) {
			while ((tuple = match(pattern)) == null) {
				try {
					lock.wait();
				}
				catch (InterruptedException e) {
					// Ignore
				}
			}
			remove(tuple);
		}
		return tuple.fields;
	}

	public String[] read(String... pattern) {
		Tuple tuple = null;
		synchronized (lock) {
			while ((tuple = match(pattern)) == null) {
				try {
					lock.wait();
				}
				catch (InterruptedException e) {
					// Ignore
				}
			}
		}
		// Return a copy of the fields
		String[] fields = new String[tuple.fields.length];
		System.arraycopy(tuple.fields, 0, fields, 0, tuple.fields.length);
		return fields;
	}

	public void put(String... fields) {
		assert(fields.length > 0);
		synchronized (lock) {
			List<Tuple> tuples = tupleMap.get(fields[0]);
			if (tuples == null) {
				tuples = new LinkedList<Tuple>();
				tupleMap.put(fields[0], tuples);
			}
			final Tuple tuple = new Tuple(fields);
			tuples.add(tuple);
			lock.notifyAll();
		}
	}
	
	Tuple match(String... pattern) {
		assert(pattern.length > 0);
		Collection<List<Tuple>> tupleLists;
		if (pattern[0] != null) {
			tupleLists = new ArrayList<List<Tuple>>(1);
			final List<Tuple> tuples = tupleMap.get(pattern[0]);
			if (tuples == null)
				return null;
			tupleLists.add(tuples);
		}
		else {
			tupleLists = tupleMap.values();
		}
		for (final List<Tuple> tuples: tupleLists) {
			for (final Tuple tuple: tuples) {
				if (tuple.match(pattern))
					return tuple;
			}
		}
		return null;
	}
	
	void remove(final Tuple tuple) {
		synchronized (lock) {
			final List<Tuple> tuples = tupleMap.get(tuple.fields[0]);
			if (tuples == null)
				return;
			tuples.remove(tuple);
		}
	}
	
	class Tuple {
		final String[] fields;
		
		Tuple(String... fields) {
			// TODO: check for null values?
			this.fields = new String[fields.length];
			System.arraycopy(fields, 0, this.fields, 0, fields.length);
		}
		
		boolean match(String... pattern) {
			if (pattern.length != fields.length)
				return false;
			for (int i = 0; i < pattern.length; ++i) {
				final String f = fields[i];
				final String p = pattern[i];
				if (p != null && !p.equals(f))
					return false;
			}
			return true;
		}
		
		@Override
		public String toString() {
			final StringBuilder str = new StringBuilder();
			str.append("(");
			for (final String field : fields) {
				str.append(field + ", ");
			}
			str.append(")");
			return str.toString();
		}
	}
}
