package moa.trident.state.jcs;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.jcs.JCS;
import org.apache.jcs.access.exception.CacheException;

import backtype.storm.tuple.Values;

import storm.trident.state.State;
import storm.trident.state.StateFactory;
import storm.trident.state.ValueUpdater;
import storm.trident.state.map.IBackingMap;
import storm.trident.state.map.MapState;
import storm.trident.state.map.SnapshottableMap;

/** 
 * An Apache JCS (http://commons.apache.org/jcs/) backed trident state implementation. 
 * @author bsp
 *
 * @param <T> - type to cache.
 */
public class JCSState <T> implements MapState<T>{
	
	private JCS m_jcs;
	
	public JCSState() throws IOException, CacheException
	{
		JCS.setConfigFilename("/moa/trident/state/jcs/jcs.properties");
		m_jcs = JCS.getInstance("sharedCache");
	}
	
	public static StateFactory create(String key) {
		return new Factory(key);
	}

	public static class Factory implements StateFactory {
		private String m_key;
		public Factory(String key) {
			m_key = key;
		}
		public State makeState(Map conf, int partitionIndex, int numPartitions) {
			try {
				return new SnapshottableMap(new JCSState<Object>(), new Values(m_key));
			} catch (IOException e) {
				throw new RuntimeException(e);
			} catch (CacheException e) {
				throw new RuntimeException(e);
			}
		}
	}

	public List<T> multiGet(List<List<Object>> keys) {
		// TODO Auto-generated method stub
		return null;
	}

	public void beginCommit(Long txid) {
		// TODO Auto-generated method stub
		
	}

	public void commit(Long txid) {
		// TODO Auto-generated method stub
		
	}

	public List<T> multiUpdate(List<List<Object>> keys,
			List<ValueUpdater> updaters) {
		// TODO Auto-generated method stub
		return null;
	}

	public void multiPut(List<List<Object>> keys, List<T> vals) {
		// TODO Auto-generated method stub
		
	}


}
