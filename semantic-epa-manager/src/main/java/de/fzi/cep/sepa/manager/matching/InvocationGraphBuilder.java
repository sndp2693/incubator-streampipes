package de.fzi.cep.sepa.manager.matching;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.clarkparsia.empire.SupportsRdfId.URIKey;
import com.rits.cloning.Cloner;

import de.fzi.cep.sepa.commons.GenericTree;
import de.fzi.cep.sepa.commons.GenericTreeNode;
import de.fzi.cep.sepa.commons.GenericTreeTraversalOrderEnum;
import de.fzi.cep.sepa.commons.Utils;
import de.fzi.cep.sepa.manager.matching.output.OutputSchemaFactory;
import de.fzi.cep.sepa.manager.matching.output.OutputSchemaGenerator;
import de.fzi.cep.sepa.manager.matching.output.SchemaOutputCalculator;
import de.fzi.cep.sepa.manager.util.TopicGenerator;
import de.fzi.cep.sepa.model.ConsumableSEPAElement;
import de.fzi.cep.sepa.model.InvocableSEPAElement;
import de.fzi.cep.sepa.model.NamedSEPAElement;
import de.fzi.cep.sepa.model.impl.EventGrounding;
import de.fzi.cep.sepa.model.impl.EventSchema;
import de.fzi.cep.sepa.model.impl.EventStream;
import de.fzi.cep.sepa.model.impl.TransportFormat;
import de.fzi.cep.sepa.model.impl.graph.SEC;
import de.fzi.cep.sepa.model.impl.graph.SECInvocationGraph;
import de.fzi.cep.sepa.model.impl.graph.SEP;
import de.fzi.cep.sepa.model.impl.graph.SEPA;
import de.fzi.cep.sepa.model.impl.graph.SEPAInvocationGraph;
import de.fzi.cep.sepa.model.vocabulary.MessageFormat;

public class InvocationGraphBuilder {

	private GenericTree<NamedSEPAElement> tree;
	private List<GenericTreeNode<NamedSEPAElement>> postOrder;
	private Cloner cloner = new Cloner();

	List<InvocableSEPAElement> graphs;

	public InvocationGraphBuilder(GenericTree<NamedSEPAElement> tree,
			boolean isInvocationGraph) {
		this.graphs = new ArrayList<>();
		this.tree = tree;
		this.postOrder = this.tree
				.build(GenericTreeTraversalOrderEnum.POST_ORDER);
		if (!isInvocationGraph)
			prepare();
	}

	private void prepare() {
		for (GenericTreeNode<NamedSEPAElement> node : postOrder) {
			if (node.getData() instanceof SEPA) {
				node.setData(new SEPAInvocationGraph((SEPA) node.getData()));
			}
			if (node.getData() instanceof SEC) {
				node.setData(new SECInvocationGraph((SEC) node.getData()));
			}
		}
	}

	public List<InvocableSEPAElement> buildGraph() {
		Iterator<GenericTreeNode<NamedSEPAElement>> it = postOrder.iterator();
		while (it.hasNext()) {
			GenericTreeNode<NamedSEPAElement> node = it.next();
			Object element = node.getData();
			if (element instanceof SEP) {
				
			} else if (element instanceof InvocableSEPAElement) {
				String outputTopic = TopicGenerator.generateRandomTopic();
				if (element instanceof SEPAInvocationGraph) {
					SEPAInvocationGraph thisGraph = (SEPAInvocationGraph) element;
					thisGraph = (SEPAInvocationGraph) buildSEPAElement(
							thisGraph, node, outputTopic);
					EventSchema outputSchema;
					EventStream outputStream = new EventStream();
					outputStream.setRdfId(makeRandomUriKey(thisGraph.getUri()
							.toString()));
					EventGrounding grounding = new EventGrounding();
					grounding.setPort(61616);
					grounding.setUri("tcp://localhost");
					grounding.setTopicName(outputTopic);
					OutputSchemaGenerator schemaGenerator = new OutputSchemaFactory(thisGraph.getOutputStrategies()).getOuputSchemaGenerator();
					if (thisGraph.getInputStreams().size() == 1) {
						outputSchema = schemaGenerator.buildFromOneStream(thisGraph.getInputStreams().get(0));
						//thisGraph.setOutputStrategies(Utils.createList(calc
						//		.getOutputStrategy()));
					} else
					{
						outputSchema = schemaGenerator.buildFromTwoStreams(thisGraph.getInputStreams().get(0), thisGraph.getInputStreams().get(1));
						//thisGraph.setOutputStrategies(Utils.createList(calc.getOutputStrategy()));
					}
					grounding
							.setTransportFormats(Utils
									.createList(getPreferredTransportFormat(thisGraph)));
					outputStream.setEventGrounding(grounding);
					outputStream.setEventSchema(outputSchema);

					thisGraph.setOutputStream(outputStream);
					graphs.add(thisGraph);
				} else {
					SECInvocationGraph thisGraph = (SECInvocationGraph) element;
					thisGraph = (SECInvocationGraph) buildSEPAElement(
							thisGraph, node, outputTopic);
					graphs.add(thisGraph);
				}
			}
		}
		return graphs;
	}

	private TransportFormat getPreferredTransportFormat(
			SEPAInvocationGraph thisGraph) {
		try {
			if (thisGraph.getInputStreams().get(0).getEventGrounding()
					.getTransportFormats() == null)
				return new TransportFormat(MessageFormat.Json);
			for (TransportFormat format : thisGraph.getInputStreams().get(0)
					.getEventGrounding().getTransportFormats()) {
				if (thisGraph.getSupportedGrounding().getTransportFormats()
						.get(0).getRdfType().containsAll(format.getRdfType()))
					return format;
			}
		} catch (Exception e) {
			return new TransportFormat(MessageFormat.Json);
		}
		// TODO
		return new TransportFormat(MessageFormat.Json);
	}

	private InvocableSEPAElement buildSEPAElement(
			InvocableSEPAElement thisGraph,
			GenericTreeNode<NamedSEPAElement> node, String outputTopic) {
		try {
			thisGraph.setRdfId(new URIKey(new URI(thisGraph.getUri() + "/"
					+ outputTopic)));

		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		for (int i = 0; i < node.getNumberOfChildren(); i++) {
			NamedSEPAElement child = node.getChildAt(i).getData();
			if (child instanceof EventStream) {
				EventStream thisStream = (EventStream) child;

				thisGraph.getInputStreams().get(i)
						.setEventSchema(thisStream.getEventSchema());
				thisGraph.getInputStreams().get(i)
						.setEventGrounding(thisStream.getEventGrounding());

			} else {
				SEPAInvocationGraph childSEPA = (SEPAInvocationGraph) child;
				thisGraph
						.getInputStreams()
						.get(i)
						.setEventSchema(
								childSEPA.getOutputStream().getEventSchema());
				thisGraph
						.getInputStreams()
						.get(i)
						.setEventGrounding(
								childSEPA.getOutputStream().getEventGrounding());
			}
		}
		return thisGraph;
	}

	private URIKey makeRandomUriKey(String uri) {
		try {
			return new URIKey(new URI(uri));
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
