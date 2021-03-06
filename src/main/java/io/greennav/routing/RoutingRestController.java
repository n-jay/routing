package io.greennav.routing;

import de.topobyte.osm4j.core.model.iface.OsmTag;
import de.topobyte.osm4j.core.model.impl.Node;
import de.topobyte.osm4j.core.model.impl.Tag;
import de.topobyte.osm4j.core.model.util.OsmModelUtil;
import io.greennav.persistence.Persistence;
import io.greennav.routing.roadgraph.impl.DistanceComputerInMetres;
import io.greennav.routing.roadgraph.iface.NodeWeightFunction;
import io.greennav.routing.router.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static de.topobyte.osm4j.core.model.util.OsmModelUtil.getTagsAsMap;

@CrossOrigin
@RestController
public class RoutingRestController {
    final private Persistence db;
    final private NodeWeightFunction weightFunction = new DistanceComputerInMetres();
    private Router router;

    @Autowired
    RoutingRestController(Persistence db) {
        this.db = db;
    }

    @RequestMapping(method = RequestMethod.GET, path = "/{algorithm}/from/{from}/to/{to}")
    List<DisplayNode> getShortestPath
            (@PathVariable(value = "from") Long fromId,
             @PathVariable(value = "to") Long toId,
             @PathVariable(value = "algorithm") String algorithm) {
        final Node fromNode = db.getNodeById(fromId);
        final Node toNode = db.getNodeById(toId);
        switch (algorithm) {
            case "dijkstra":
                router = new DijkstraRouter(db, weightFunction);
                break;
            case "astar":
                router = new AStarRouter(db, weightFunction);
                break;
            case "bidirectional-dijkstra":
                router = new BidirectionalDijkstraRouter(db, weightFunction);
                break;
            case "contraction-hierarchies":
                router = new ContractionHierarchiesRouter(db, weightFunction);
                break;
            default:
                break;
        }

        List<DisplayNode> results = new ArrayList<>();

        for (Node n : router.getShortestPath(fromNode, toNode).getRoute())
            results.add(new DisplayNode("", n.getId(),
                    n.getLongitude(), n.getLatitude()));

        return results;
    }

    @RequestMapping(method = RequestMethod.GET, path = "/search/{nodeName}")
    List<DisplayNode> searchNode(@PathVariable(value = "nodeName") String nodeName) {
        List<DisplayNode> results = new ArrayList<>();

        for (Node n : db.queryNodes("name", nodeName)) {
            Map tags = OsmModelUtil.getTagsAsMap(n);

            String displayName = (String) tags.get("name");
            results.add(new DisplayNode(displayName, n.getId(),
                    n.getLongitude(), n.getLatitude()));
        }

        return results;
    }
}
