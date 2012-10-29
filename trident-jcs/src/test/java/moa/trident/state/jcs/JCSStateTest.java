package moa.trident.state.jcs;

import org.junit.Before;
import org.junit.Test;

import storm.trident.Stream;
import storm.trident.TridentTopology;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.LocalDRPC;
import backtype.storm.testing.TestWordSpout;
import backtype.storm.tuple.Fields;

import junit.framework.TestCase;

public class JCSStateTest extends TestCase {
	
	JCSState m_state;
	
	@Before
	public void setUp() throws Exception
	{
		m_state = new JCSState<Object>("test");
	}
	
	@Test
	public void test()
	{
		LocalCluster cls = new LocalCluster();
		LocalDRPC local = new LocalDRPC();
		Config conf = new Config();
		
		TridentTopology topology = new TridentTopology();
		conf.put("topology.spout.max.batch.size", 5);
		Stream s = topology.newStream("sample" , new TestWordSpout());
		s.persistentAggregate(JCSState.create("magic"), new Fields("word"),new AggregateMe(), new Fields("word_agg"));
		
		
		
		cls.submitTopology("test",  conf, topology.build()	);
		try {
			Thread.sleep(10000000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
