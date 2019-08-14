import java.util.ArrayList;
import java.util.Arrays;
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
	
	// 初始化模拟网络
	private void initNet() {
		nodes.clear();
		taskQueue.clear();
		
		Set<Integer> indexPool = new HashSet<Integer>();
		for (int i = 0; i < Config.NODE_NUM; i++) {
			int index = (int) (Math.random() * Config.INDEX_RANGE);
			while(indexPool.contains(index)) {
				index = (int) (Math.random() * Config.INDEX_RANGE);
			}
			indexPool.add(index);
			Node n = new Node(i, index, nodes, taskQueue);
			nodes.add(n);
		}
		
		for (int i = 0; i < Config.NODE_NUM; i++) {
			List<Integer> adj = new ArrayList<Integer>();
			nodes.get(i).setAdj(adj);
			// 随机生成网络连接
			double possible[] = new double[Config.NODE_NUM];
			double F[] = new double[Config.NODE_NUM];
			double total = 0.;
			for (int j = 0; j < Config.NODE_NUM; j++) {
				if (j == i) possible[j] = 0;
				else {
					// 由于是环形，因此距离计算，按照环形计算
	                double dis = Math.min(Math.abs(nodes.get(i).index - nodes.get(j).index),
	                		Math.abs(nodes.get(i).index - nodes.get(j).index + Config.INDEX_RANGE));
	                dis = Math.min(dis,
	                		Math.abs(nodes.get(i).index - nodes.get(j).index - Config.INDEX_RANGE));
	                possible[j] = 1 / (dis * dis);
	                total += possible[j];
	            }
			}
			
			for (int j = 0; j < Config.NODE_NUM; j++) {
				if (j == 0)
					F[j] = possible[j] / total;
				else
					F[j] = F[j-1] + possible[j] / total;
			}
			
			for (int j = 0; j < Config.LINK_NUM; j++) {
				double pos = Math.random();
				int temp = lower_bound(F, 0, Config.NODE_NUM - 1, pos);
				if (temp != i && !adj.contains(temp)) {
					adj.add(temp);
					possible[temp] = 0;
				} else {
					j--;
					total = 0;
					for (int x = 0; x < Config.NODE_NUM; x++) {
						total += possible[x];
					}
					Arrays.fill(F, 0.);
					for (int x = 0; x < Config.NODE_NUM; x++) {
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
				n.transferTxPackage();
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	
	public void simulation() {
		double aveTxPackageNum = 0.;
		double aveCoverage = 0.;
		double aveDelay = 0.;
		
		for (int i = 0; i < 10; i++) {
			double txPackageNum = 0.;
			double coverage = 0.;
			double delay = 0.;
			
			initNet();	// 随机生成一次连接
			System.out.println("InitNet finished");
			
			for (int j = 0; j < Config.TX_NUM; j++) {
				Statistic s = new Statistic();
				int fromId = (int)(Math.random() * Config.NODE_NUM);
				Node n = nodes.get(fromId);
				TxPackage tx = new TxPackage(j, fromId, s, n.index - Config.INDEX_RANGE>>1, n.index + Config.INDEX_RANGE>>1, 1);
				n.setTx(tx);
				taskQueue.add(n);
				run();
				txPackageNum += tx.getStatistic().getTxPackageNum();
				coverage += tx.getStatistic().getCoverage();
				delay += tx.getStatistic().getDelay();
			}
			
			aveTxPackageNum += txPackageNum / Config.TX_NUM;
			aveCoverage += coverage / Config.TX_NUM;
			aveDelay += delay / Config.TX_NUM;
			System.out.println("Finished " + 100 * i / 10 + "%");
		}
		
		aveTxPackageNum /= 10;
		aveCoverage /= 10;
		aveDelay /= 10;
		
		String result = " NodeCount: " + Config.NODE_NUM + " "
				+ "LinkNum: " + Config.LINK_NUM + " "
				+ "TxPackageNum: " + aveTxPackageNum + " "
				+ "Delay: " + aveDelay + " "
				+ "coverage: " + aveCoverage + "%";
		System.out.println(result);
	}
}
