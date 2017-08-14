package br.ufu.facom.ppgco.ia.gephi.plugin.metric;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.Table;
import org.gephi.statistics.plugin.GraphDistance;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Stack;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import org.gephi.graph.api.*;
import org.gephi.statistics.plugin.ChartUtils;
import org.gephi.statistics.spi.Statistics;
import org.gephi.utils.TempDirUtils;
import org.gephi.utils.TempDirUtils.TempDir;
import org.gephi.utils.progress.Progress;
import org.gephi.utils.progress.ProgressTicket;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.openide.util.NbBundle;

/**
 *
 * @author getulio
 */
public class BridgingCentralityMetric extends GraphDistance implements Statistics {
    
    private static final Logger logger = Logger.getLogger(BridgingCentralityMetric.class.getName());
    
    public static final String BRIDGING_CENTRALITY = "bridgingcentrality";
    public static final String BRIDGING_COEFFICIENT = "bridgingcoefficient";
    
    // relatorio de execucao
    private String report = "";

    // controle de execucao, com possibilidade de interrupcao pelo usuario
    private boolean cancelled = false;
    private ProgressTicket progressTicket;
    
    // parametros de entrada do usuario
    private boolean directed;
    private boolean normalized;
    
    // tomada de tempo de execucao
    private LocalDateTime start = null;
    private LocalDateTime stop_betweeness = null;
    private LocalDateTime stop_bridging = null;
    
    // estatisticas calculadas pelo algoritmo
    private double[] betweenness;
    private double[] bridging_cent;
    private double[] bridging_coef;
    
    // variaveis globais
    private int N;
    
    /**
     * Bridging Centrality algorithm ....
     *
     * @param graphModel
     */
    @Override
    public void execute(GraphModel graphModel) {

        directed = graphModel.isDirected();

        Graph graph = null;
        if ( directed ) {
            graph = graphModel.getDirectedGraphVisible();
        } else {
            graph = graphModel.getUndirectedGraphVisible();
        }
        
	////////////
//	
//	Table nodeTable = graphModel.getNodeTable();
//
//	boolean betweenn_cent_col_exists = nodeTable.hasColumn(BETWEENNESS);
//
//	if (betweenn_cent_col_exists) {
//
//	    logger.log(java.util.logging.Level.WARNING, "Valores de betweennes centrality já existem...");
//
//	    msg = NbBundle.getMessage(BridgingCentralityMetric.class, "BridgingCentralityMetric.bwexists.message");
//
//	    if (JOptionPane.YES_OPTION != JOptionPane.showConfirmDialog(null, msg, "Confirm", JOptionPane.YES_NO_OPTION)) {
//
//		logger.log(java.util.logging.Level.WARNING, "Utilizando os valores de betweennes centrality já existentes...");
//
//		start = LocalDateTime.now();
//		
//		initializeAttributeColunms(graphModel);
//
//		N = graph.getNodeCount();
//
//		betweenness = new double[N];
//		bridging_cent = new double[N];
//		bridging_coef = new double[N];
//
//		HashMap<Node, Integer> indicies = createIndiciesMap(graph);
//		
//		for (Node n : graph.getNodes()) {
//		    
//		    int idx = indicies.get(n);
//		    
//		    betweenness[idx] = (Double) n.getAttribute(BETWEENNESS);
//		    bridging_coef[idx] = (Double) bridging_coeficient(graph, n);
//		    bridging_cent[idx] = betweenness[idx] * bridging_coef[idx];
//		    
//		    n.setAttribute(BRIDGING_COEFFICIENT, bridging_coef[idx]);
//		    n.setAttribute(BRIDGING_CENTRALITY, bridging_cent[idx]);
//		}
//
//		stop_betweeness = start;
//		stop_bridging = LocalDateTime.now();
//		
//		makeReport(graph, indicies);
//
//	    } else {
//
//		logger.log(java.util.logging.Level.WARNING, "Recalculando todos os valores de betweennes centrality...");
//
//		// recalculando valores de betweenness
//		execute(graph);
//	    }
//
//	} else {
//
//	    // betweenness nao existem
//	    logger.log(java.util.logging.Level.WARNING, "Calculando todos os valores de betweennes centrality...");
//
//	    execute(graph);
//	}

	
	logger.log(java.util.logging.Level.WARNING, "Recalculando todos os valores de betweennes centrality...");
	execute(graph);
	logger.log(java.util.logging.Level.INFO, "Calculo de Bridging Centrality concluido com sucesso.");
    }
    
    @Override
    public void execute(Graph graph) {
        
	cancelled = false;

        graph.readLock();
	
        try {
	    
            initializeAttributeColunms(graph.getModel());
            
            N = graph.getNodeCount();

            betweenness = new double[N];
            bridging_cent = new double[N];
            bridging_coef = new double[N];

            HashMap<Node, Integer> indicies = createIndiciesMap(graph);
            
            start = LocalDateTime.now();
            
            betweenness = calculateBetweeness(graph, indicies, directed, normalized);
            
            stop_betweeness = LocalDateTime.now();
            
            saveCalculatedValues(graph, indicies, betweenness); // calcula o bridging coefficient e o bridging centrality
            
            stop_bridging = LocalDateTime.now();
            
            // -----------
            if (cancelled) {
                String msg = NbBundle.getMessage(BridgingCentralityMetric.class, "BridgingCentralityMetric.alert.message");
                String title = NbBundle.getMessage(BridgingCentralityMetric.class, "BridgingCentralityMetric.alert.title");
                JOptionPane.showMessageDialog(null, msg, title, JOptionPane.WARNING_MESSAGE);
                logger.log(java.util.logging.Level.WARNING, msg);
            } else {
		Duration diff_1 = Duration.between(start, stop_betweeness);
		Duration diff_2 = Duration.between(stop_betweeness, stop_bridging);
		String duration = NbBundle.getMessage(BridgingCentralityMetric.class, "BridgingCentralityMetric.report.duration");

		report += "<b>" + duration + " </b> : " + diff_1.getNano() + " ns (Betweenness) <br/>";
		report += "<b>" + duration + " </b> : " + diff_2.getNano() + " ns (Bridging) <br/>";
		report += "<b>" + duration + " </b> : " + (diff_1.getNano() + diff_2.getNano()) + " ns (Total) <br/>";

		report += "<br/>";
		report += "<table>";
		report += "<tr>";
		report += "<td>ID</td><td>Label</td><td>Degree</td><td>Betweeness</td><td>Bridging Coefficient</td><td>Bridging Centrality</td>";
		report += "</tr>";
		int s_index = 0;
		int count = 0;
		for (Node s : graph.getNodes()) {

		    if ( count == 20 ) {
			break;
		    } 
		    count++;

		    s_index = indicies.get(s);
		    report += "<tr>";
		    report += "<td>" + (String) s.getId() + "</td>"
			      + "<td>" + s.getLabel() + "</td>"
			      + "<td>" + graph.getDegree(s) + "</td>"
			      + "<td>" + String.format("%.5f", betweenness[s_index]) + "</td>"
			      + "<td>" + String.format("%.5f", bridging_coef[s_index]) + "</td>"
			      + "<td>" + String.format("%.5f", bridging_cent[s_index]) + "</td>";
		    report += "</tr>";
		}
		report += "</table>";

		report += "<br/>";
            }
            // -----------
            
        } finally {
            graph.readUnlock();
        }
    }
    
    /**
     * Inicializa a tabela de nós
     * 
     * @param graphModel 
     */
    private void initializeAttributeColunms(GraphModel graphModel) {
        
        Table nodeTable = graphModel.getNodeTable();
        
        if ( ! nodeTable.hasColumn(BETWEENNESS) ){
            nodeTable.addColumn(BETWEENNESS, "Betweenness Centrality", Double.class, new Double(0));
        }
        
        if ( ! nodeTable.hasColumn(BRIDGING_COEFFICIENT) ){
            nodeTable.addColumn(BRIDGING_COEFFICIENT, "Bridging Coefficient", Double.class, new Double(0));
        }
        
        if ( ! nodeTable.hasColumn(BRIDGING_CENTRALITY) ){
            nodeTable.addColumn(BRIDGING_CENTRALITY, "Bridging Centrality", Double.class, new Double(0));
        }
    }
    
    @Override
    public HashMap<Node, Integer> createIndiciesMap(Graph graph) {
        
        HashMap<Node, Integer> indicies = new HashMap<Node, Integer>();
        
        int index = 0;
        for (Node n : graph.getNodes()) {
            indicies.put(n, index);
            index++;
        }
        
        return indicies;
    }

    /**
     * 
     * @param graph
     * @param indicies
     * @param directed
     * @param normalized
     * 
     * @return array with betweeness statistics
     */
    private double[] calculateBetweeness(Graph graph, HashMap<Node, Integer> indicies, boolean directed, boolean normalized) {
        
        double[] nodeBetweenness = new double[ N ];

        int count = 0;

        Progress.start(progressTicket, N);
        
        Stack<Node> S = null;
        LinkedList<Node>[] P  = null;
        double[] theta = null;
        int[] d = null;
        int s_index = 0;
        LinkedList<Node> Q = null;
        
        Node v = null;
        int v_index = 0;
        EdgeIterable edgeIter = null;
        
        Node reachable = null;
        int r_index = 0;
        
        double[] delta = null;
        Node w = null;
        int w_index = 0;
        ListIterator<Node> it = null;
        
        Node u = null;
        int u_index = 0;
        
        for (Node s : graph.getNodes()) {
            
            S= new Stack<Node>();

            P = new LinkedList[ N ];
            
            theta = new double[ N ];
            
            d = new int[ N ];

            s_index = indicies.get(s);

            setInitParametetrsForNode(s, P, theta, d, s_index,  N );

            Q = new LinkedList<Node>();
            Q.addLast(s);
            while ( ! Q.isEmpty() ) {
                v = Q.removeFirst();
                S.push(v);
                v_index = indicies.get(v);

                edgeIter = getEdgeIter(graph, v, directed);

                for (Edge edge : edgeIter) {
                    
                    reachable = graph.getOpposite(v, edge);

                    r_index = indicies.get(reachable);
                    
                    if (d[r_index] < 0) {
                        Q.addLast(reachable);
                        d[r_index] = d[v_index] + 1;
                    }
                    
                    if (d[r_index] == (d[v_index] + 1)) {
                        theta[r_index] = theta[r_index] + theta[v_index];
                        P[r_index].addLast(v);
                    }
                }
            }
            
            // -------------
            
            delta = new double[ N ];
            while ( ! S.empty() ) {
                
                w = S.pop();
                
                w_index = indicies.get(w);
                
                it = P[w_index].listIterator();
                while (it.hasNext()) {
                    u = it.next();
                    u_index = indicies.get(u);
                    delta[u_index] += (theta[u_index] / theta[w_index]) * (1 + delta[w_index]);
                }
                
                if (w != s) {
                    nodeBetweenness[w_index] += delta[w_index];
                }
            }
            
            count++;
            
            if (cancelled) {
                return nodeBetweenness;
            }
            
            Progress.progress(progressTicket, count);
        }
        
        // corrige e normaliza o resultado da betweenness
        calculateCorrection(graph, indicies, nodeBetweenness, directed, normalized);

        return nodeBetweenness;        
    }

//    private void makeReport(Graph graph, HashMap<Node, Integer> indicies) {
//	
//	Duration diff_1 = Duration.between(start, stop_betweeness);
//
//	Duration diff_2 = Duration.between(stop_betweeness, stop_bridging);
//	String duration = NbBundle.getMessage(BridgingCentralityMetric.class, "BridgingCentralityMetric.report.duration");
//
//	report += "<b>" + duration + " </b> : " + diff_1.getNano() + " ns (Betweenness) <br/>";
//	report += "<b>" + duration + " </b> : " + diff_2.getNano() + " ns (Bridging) <br/>";
//	report += "<b>" + duration + " </b> : " + (diff_1.getNano() + diff_2.getNano()) + " ns (Total) <br/>";
//
//	report += "<br/>";
//	report += "<table>";
//	report += "<tr>";
//	report += "<td>ID</td><td>Label</td><td>Degree</td><td>Betweeness</td><td>Bridging Coefficient</td><td>Bridging Centrality</td>";
//	report += "</tr>";
//	int s_index = 0;
//	int count = 0;
//	for (Node s : graph.getNodes()) {
//	    
//	    if ( count == 20 ) {
//		break;
//	    } 
//	    count++;
//	    
//	    s_index = indicies.get(s);
//	    report += "<tr>";
//	    report += "<td>" + (String) s.getId() + "</td>"
//		      + "<td>" + s.getLabel() + "</td>"
//		      + "<td>" + graph.getDegree(s) + "</td>"
//		      + "<td>" + String.format("%.5f", betweenness[s_index]) + "</td>"
//		      + "<td>" + String.format("%.5f", bridging_coef[s_index]) + "</td>"
//		      + "<td>" + String.format("%.5f", bridging_cent[s_index]) + "</td>";
//	    report += "</tr>";
//	}
//	report += "</table>";
//
//	report += "<br/>";
//    }
    
    /**
     * Salva as estatisticas calculadas como atributos de cada nó
     * 
     * @param graph
     * @param indicies
     * @param nodeBetweenness 
     */
    private void saveCalculatedValues(Graph graph, HashMap<Node, Integer> indicies, double[] nodeBetweenness) {
        
        for (Node s : graph.getNodes()) {
            
            int s_index = indicies.get(s);
            
            bridging_coef[s_index] = bridging_coeficient(graph, s);
            bridging_cent[s_index] = bridging_coef[s_index] * betweenness[s_index];

            s.setAttribute(BETWEENNESS, nodeBetweenness[s_index]);
            s.setAttribute(BRIDGING_COEFFICIENT, bridging_coef[s_index]);
            s.setAttribute(BRIDGING_CENTRALITY, bridging_cent[s_index]);
        }
    }

    /**
     * Aplica as correcoes/normalizacoes nos nós, para a betweeness
     * 
     * @param graph
     * @param indicies
     * @param nodeBetweenness
     * @param directed
     * @param normalized 
     */
    private void calculateCorrection(Graph graph, HashMap<Node, Integer> indicies, double[] nodeBetweenness, boolean directed, boolean normalized) {

        int s_index = 0;
        for (Node s : graph.getNodes()) {

            s_index = indicies.get(s);
            
            // corrige o resultado, para o caso de rede nao dirigida
            if (! directed) {
                nodeBetweenness[s_index] /= 2;
            }
            
            // normaliza o resultado
            if (normalized) {
                nodeBetweenness[s_index] /= directed ? (N - 1) * (N - 2) : (N - 1) * (N - 2) / 2;
            }
        }
    }

    private void setInitParametetrsForNode(Node s, LinkedList<Node>[] P, double[] theta, int[] d, int index, int n) {
        
        for (int j = 0; j < n; j++) {
            P[j] = new LinkedList<Node>();
            theta[j] = 0;
            d[j] = -1;
        }
        
        theta[index] = 1;
        d[index] = 0;
    }
    
    private EdgeIterable getEdgeIter(Graph graph, Node v, boolean directed) {
        
        if (directed) {
            return ((DirectedGraph) graph).getOutEdges(v);
        } 
        
        return graph.getEdges(v);
    }
    
    
    
    /**
     * Should return plain text or HTML text that describe the algorithm
     * execution.
     *
     * @return
     */
    @Override
    public String getReport() {
        
        String htmlIMG1 = "";
        String htmlIMG2 = "";
        String htmlIMG3 = "";
        
        try {
            TempDir tempDir = TempDirUtils.createTempDir();
            htmlIMG1 = createImageFile(tempDir, bridging_cent, "Bridging Centrality Distribution", "Value", "Count");
            htmlIMG2 = createImageFile(tempDir, bridging_coef, "Bridging Coefficient Distribution", "Value", "Count");
            htmlIMG3 = createImageFile(tempDir, betweenness, "Betweenness Centrality Distribution", "Value", "Count");
        } catch (IOException ex) {
            
            String msg = NbBundle.getMessage(BridgingCentralityMetric.class, "BridgingCentralityMetric.error.message");
            String title = NbBundle.getMessage(BridgingCentralityMetric.class, "BridgingCentralityMetric.error.title");
            JOptionPane.showMessageDialog(null, msg, title, JOptionPane.ERROR_MESSAGE);
            
            logger.log(java.util.logging.Level.SEVERE, msg, ex);
        }

        String report_title = NbBundle.getMessage(BridgingCentralityMetric.class, "BridgingCentralityMetric.report.title");
        String params = NbBundle.getMessage(BridgingCentralityMetric.class, "BridgingCentralityMetric.report.params");
        String param_1 = NbBundle.getMessage(BridgingCentralityMetric.class, "BridgingCentralityMetric.report.params.par_1");
        String value_1_1 = NbBundle.getMessage(BridgingCentralityMetric.class, "BridgingCentralityMetric.report.params.par_1.value_1");
        String value_1_2 = NbBundle.getMessage(BridgingCentralityMetric.class, "BridgingCentralityMetric.report.params.par_1.value_2");
        String value_2_1 = NbBundle.getMessage(BridgingCentralityMetric.class, "BridgingCentralityMetric.report.params.par_2.value_1");
        String value_2_2 = NbBundle.getMessage(BridgingCentralityMetric.class, "BridgingCentralityMetric.report.params.par_2.value_2");
        String results = NbBundle.getMessage(BridgingCentralityMetric.class, "BridgingCentralityMetric.report.results");
        
        String param_2 = NbBundle.getMessage(BridgingCentralityMetric.class, "BridgingCentralityMetric.report.params.par_2");
        
        String rep = "<HTML> <BODY> <h1>" + report_title + "</h1> "
                   + "<hr>"
                   + "<br>"
                   + "<h2> " + params + " : </h2>"
                   + param_1 + " : " + (directed ? value_1_1 : value_1_2) + "<br />"
                   + param_2 + " : " + (normalized ? value_2_1 : value_2_2) + "<br />"
                   + "<br /> <h2> " + results + ": </h2>";
        
        rep += report;
        
        String algorithms = NbBundle.getMessage(BridgingCentralityMetric.class, "BridgingCentralityMetric.report.algorithm");
        String credits = NbBundle.getMessage(BridgingCentralityMetric.class, "BridgingCentralityMetric.report.credits");
        String contact = NbBundle.getMessage(BridgingCentralityMetric.class, "BridgingCentralityMetric.report.contact");
        
        rep += "<br /><br />"
            + htmlIMG1 + "<br /><br />"
            + htmlIMG2 + "<br /><br />"
            + htmlIMG3 + "<br /><br />"
            + "<br /><br />"
            + "<h2>" + algorithms + ": </h2>"
            + "<ul>"
            + "<li>Ulrik Brandes, <b>A Faster Algorithm for Betweenness Centrality</b>, in Journal of Mathematical Sociology 25(2):163-177, (2001)</li>"
            + "<li>Hwang, W., Cho, Y. R., Zhang, A., & Ramanathan, M. (2006, March). <b>Bridging centrality: identifying bridging nodes in scale-free networks</b>. In Proceedings of the 12th ACM SIGKDD international conference on Knowledge discovery and data mining (pp. 20-23).</li>"
            + "</ul>"
            + "<b>" + credits + ": </b>"
            + "<ul>"
            + "<li>Get&uacute;lio de Morais Pereira, 2017&copy;, </li>"
            + "<ul>"
            + "<li>" + contact + " : getulio.pereira@gmail.com </li>"
            + "<li> URL : https://www.researchgate.net/profile/Getulio_De_Morais_Pereira</li>"
            + "</ul>"
            + "<ul>"
            + "</BODY> </HTML>";

        logger.log(java.util.logging.Level.INFO, "Execucao concluida com sucesso.......");
        
        return rep;
    }
    
    private String createImageFile(TempDir tempDir, double[] pVals, String pName, String pX, String pY) {
        
        //distribution of nodeIDs
        Map<Double, Integer> dist = new HashMap<Double, Integer>();
        
        for (int i = 0; i < N; i++) {
            Double d = pVals[i];
            if (dist.containsKey(d)) {
                Integer v = dist.get(d);
                dist.put(d, v + 1);
            } else {
                dist.put(d, 1);
            }
        }

        //Distribution series
        XYSeries dSeries = ChartUtils.createXYSeries(dist, pName);

        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(dSeries);

        JFreeChart chart = ChartFactory.createXYLineChart(
                pName,
                pX,
                pY,
                dataset,
                PlotOrientation.HORIZONTAL,
                true,
                false,
                false);
        
        chart.removeLegend();
        ChartUtils.decorateChart(chart);
        ChartUtils.scaleChart(chart, dSeries, normalized);
        
        return ChartUtils.renderChart(chart, pName + ".png");
    }

    /**
     * Calcula o bridging coeficient de um dado vertice
     *
     * @param graph
     * @param node
     *
     * @return bridging coefficient value
     */
    private static double bridging_coeficient(Graph graph, Node node) {
        
        double n = 1.0 / graph.getDegree(node);

        double d = 0.0;
        Iterator<Node> it = graph.getNeighbors(node).iterator();
        while( it.hasNext() ) {
            Node t = (Node) it.next();
            d += 1.0 / graph.getDegree( t );
        }

        return n / d;
    }

    /**
     * cancelar a execucao do algoritmo
     */
    public boolean cancel() {
        this.cancelled = true;
        return true;
    }

    public void setProgressTicket(ProgressTicket progressTicket) {
        this.progressTicket = progressTicket;
    }
    
    public boolean isDirected() {
        return directed;
    }

    public void setDirected(boolean directed) {
        this.directed = directed;
    }  
    
    public boolean isNormalized() {
        return normalized;
    }

    public void setNormalized(boolean normalized) {
        this.normalized = normalized;
    }  
}
