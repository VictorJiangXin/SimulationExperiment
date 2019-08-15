import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;

/**
 * 表示每个节点
 * 主要有：
 * 1. 节点标号
 * 2. 节点坐标
 * 3. 节点的邻节点
 * 4. 当前需要转发的交易包
 * 5. 节点池
 * 6. 任务池
 * 
 * 需要实现的功能是：
 * 传递交易包，将执行任务的节点放入到任务矩阵中
 * @author xinjiang
 *
 */
public class Node {
	private int nodeId;
	private List<Integer> adjointNodes;		// 邻节点
	private TxPackage txPackage;	// 本次传递的交易包
	public int index;
	private List<Node> nodes;	// 节点模板池
	Queue<Node> taskQueue;	// 任务队列
	
	
	public Node(int nodeId, int index, List<Node> nodes, Queue<Node> taskQueue) {
		this.nodeId = nodeId;
		this.index = index;
		this.nodes = nodes;
		this.taskQueue = taskQueue;
	}
	
	
	public int getNodeId() {
		return nodeId;
	}
	
	
	public TxPackage getTxPackage() {
		return txPackage;
	}
	
	
	public void setTx(TxPackage txPackage) {
		this.txPackage = txPackage;
	}
	
	
	public void setAdj(List<Integer> adjointNodes) {
		this.adjointNodes = adjointNodes;
	}
	
	
	public Node copyNode(TxPackage tx) {
		Node n = new Node(nodeId, index, nodes, taskQueue);
		n.setAdj(adjointNodes);
		n.setTx(tx);
		return n;
	}
	
	
	// 将交易传播出去
	public void transferTxPackage() {
		// 更新统计数据,如果未收到过交易返回true
		if(!txPackage.getStatistic().update(this)) return;
		int districtNum = Config.PARTITION_NUM;
		int leftBound = txPackage.getLeftBound();
		int rightBound = txPackage.getRightBound();
		
		List<Pair> optionNodes = new ArrayList<Pair>();	// 在需要传送区域内的候选节点
		for (int adjointId : adjointNodes) {
			int index = nodes.get(adjointId).index;
			// 在放进备选队列时，要进行细致的分类，从而实现循环，要进行坐标转换
			if (adjointId != nodeId) {
				if (leftBound < 0 && rightBound < 0) {
					// 坐标在负范围，就添加负坐标
					index -= Config.INDEX_RANGE;
					if (index >= leftBound && index <= rightBound) 
						optionNodes.add(new Pair(adjointId, index));
				} else if ((leftBound >= 0 && leftBound < Config.INDEX_RANGE) && 
				(rightBound >= 0 && rightBound < Config.INDEX_RANGE)) {
					// 坐标都在正常范围内，添加正常nodeId
					if (index >= leftBound && index <= rightBound)
						optionNodes.add(new Pair(adjointId, index));
				} else if (leftBound >= Config.INDEX_RANGE && rightBound >= Config.INDEX_RANGE) {
					// 坐标都在右循环范围
					index += Config.INDEX_RANGE;
					if (index >= leftBound && index <= rightBound)
						optionNodes.add(new Pair(adjointId, index));
				} else if (leftBound < 0 && rightBound >= 0 && rightBound < Config.INDEX_RANGE) {
					// 左循环，左边负范围，右边正常范围
					if (index - Config.INDEX_RANGE >= leftBound && index - Config.INDEX_RANGE < 0)
						optionNodes.add(new Pair(adjointId, index - Config.INDEX_RANGE));
					else if (index >= 0 && index <= rightBound)
						optionNodes.add(new Pair(adjointId, index));
				} else if (leftBound >= 0 && leftBound < Config.INDEX_RANGE && rightBound >= Config.INDEX_RANGE) {
					// 右循环，左边正常范围，右边循环范围
					if (index >= leftBound && index < Config.INDEX_RANGE)
						optionNodes.add(new Pair(adjointId, index));
					else if (index + Config.INDEX_RANGE >= Config.INDEX_RANGE 
							&& index + Config.INDEX_RANGE <= rightBound)
						optionNodes.add(new Pair(adjointId, index + Config.INDEX_RANGE));
				}
			}
		}	
		// 根据节点坐标，进行升序
		Collections.sort(optionNodes);
		if (!optionNodes.isEmpty()) {
			int transferNum = optionNodes.size();
			int partNum = Math.max(transferNum / districtNum, 1);
			int j = 0;
			int l = leftBound;
			int r;
			for (int i = 0; i < districtNum  && j < transferNum; i++) {
				if (i == districtNum - 1 || j == transferNum - partNum)
					r = rightBound;
				else
					r = (optionNodes.get(j).index + optionNodes.get(j + partNum).index) / 2;
				
				int dis = Integer.MAX_VALUE;
				int selectNodeId = -1;
				for (int k = j; k < j + partNum && k < transferNum; k++) {
					int tempDis = Math.abs(optionNodes.get(k).index - (r + l) / 2);
					if (tempDis < dis) {
						dis = tempDis;
						selectNodeId = optionNodes.get(k).nodeId;
					}
				}
				if (selectNodeId != -1) {
					Node template = nodes.get(selectNodeId); //首先获取相应的节点模板
					// 根据节点模板，产生一个新的节点对象，设置该节点需要传递的交易包
					// 新的需要传递的交易包只是需要改变域的坐标范围。将该对象加入到任务队列中执行
					Node n = template.copyNode(txPackage.copyTxPackage(l, r));	
					taskQueue.add(n);
				}
				j += partNum;
				l = r + 1;
			}
			
		}
	}
	
}
