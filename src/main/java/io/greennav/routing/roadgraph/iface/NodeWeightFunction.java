package io.greennav.routing.roadgraph.iface;

import de.topobyte.osm4j.core.model.impl.Node;
import java.util.function.BiFunction;

public interface NodeWeightFunction extends BiFunction<Node, Node, Double> {}
