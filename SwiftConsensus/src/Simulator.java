import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

public class Simulator {
	List<Node> nodes;
	Queue<Node> taskQueue;
	
	public Simulator() {
		nodes = new ArrayList<Node>();
		taskQueue = new LinkedList<Node>();
	}
	
	private int lower_bound(double[] nums, int begin, int end, double value) {
	    while (begin < end) {
	        int mid = begin + (end - begin) / 2;
	        if (nums[mid] < value) {
	            begin = mid + 1;
	        } else {
	            end = mid;
	        }
	    }
	    return begin;
	}
	
	private void init() {
		Set<Integer> indexPool = new HashSet<Integer>();
		for (int i = 0; i < Config.nodeNum; i++) {
			int index = (int) (Math.random() * Config.indexRange);
			while(indexPool.contains(index)) {
				index = (int) (Math.random() * Config.indexRange);
			}
			indexPool.add(index);
			Node n = new Node(nodes, index, i, taskQueue);
			nodes.add(n);
		}
		
		for (int i = 0; i < Config.nodeNum; i++) {
			List<Integer> adj = new ArrayList<Integer>();
			nodes.get(i).setAdj(adj);
			// 随机生成网络连接
			double possible[] = new double[Config.nodeNum];
			double F[] = new double[Config.nodeNum];
			double total = 0.;
			for (int j = 0; j < Config.nodeNum; j++) {
				if (j == i) possible[j] = 0;
				else {
					// 由于是环形，因此距离计算，按照环形计算
	                double dis = Math.min(Math.abs(nodes.get(i).index - nodes.get(j).index),
	                		Math.abs(nodes.get(i).index - nodes.get(j).index + Config.indexRange));
	                dis = Math.min(dis,
	                		Math.abs(nodes.get(i).index - nodes.get(j).index - Config.indexRange));
	                possible[j] = 1 / (dis * dis);
	                total += possible[j];
	            }
			}
			
			for (int j = 0; j < Config.nodeNum; j++) {
				if (j == 0)
					F[j] = possible[j] / total;
				else
					F[j] = F[j-1] + possible[j] / total;
			}
			
			for (int j = 0; j < Config.linkNum; j++) {
				double pos = Math.random();
				int temp = lower_bound(F, 0, Config.nodeNum - 1, pos);
				if (temp != i && !adj.contains(temp)) {
					adj.add(temp);
					possible[temp] = 0;
				} else {
					j--;
					total = 0;
					for (int x = 0; x < Config.nodeNum; x++) {
						total += possible[x];
					}
					Arrays.fill(F, 0.);
					for (int x = 0; x < Config.nodeNum; x++) {
	                    if (x != 0)
	                        F[x] = possible[x] / total + F[x-1];
	                    else
	                        F[x] = possible[x] / total;
	                }
				}
			}
		}
	}
	
	private void run() {
		while(!taskQueue.isEmpty()) {
			try {
				Node n = taskQueue.peek();
				taskQueue.remove();
				n.transferTx();
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public void simulation() {
		init();
		double hopCount = 0.;
		double coverage = 0.;
		double hop = 0.;
		for (int i = 0; i < Config.txNum; i++) {
			if (i % 10 == 0) System.out.println("Transfer tx " + i);
			Statistic s = new Statistic();
			int fromId = (int)(Math.random() * Config.nodeNum);
			Node n = nodes.get(fromId);
			TxPackage tx = new TxPackage(i, fromId, s, n.index - Config.indexRange / 2,
					n.index + Config.indexRange / 2, 1);
			n.setTx(tx);
			taskQueue.add(n);
			run();
			hopCount += tx.getStatistic().getHopCount();
			coverage += tx.getStatistic().getCoverage();
			hop += tx.getStatistic().getHop();
		}
		hopCount /= Config.txNum;
		coverage /= Config.txNum;
		hop /= Config.txNum;
		String result = " NodeCount: " + Config.nodeNum + " "
				+ "LinkNum: " + Config.linkNum + " "
				+ "hop count: " + hopCount + " "
				+ "Max hop: " + hop + " "
				+ "coverage: " + coverage + "%";
		System.out.println(result);
	}
}
