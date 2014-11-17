/*
 * Here comes the text of your license
 * Each line should be prefixed with  * 
 */
package nars.gui.output.graph;

/**
 *
 * @author me
 */

import automenta.vivisect.dimensionalize.FastOrganicLayout;
import automenta.vivisect.graph.AnimatingGraphVis;
import automenta.vivisect.graph.GraphDisplay;
import automenta.vivisect.graph.GraphDisplays;
import automenta.vivisect.swing.NSlider;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import nars.core.EventEmitter.EventObserver;
import nars.core.Events.FrameEnd;
import nars.core.Events.ResetEnd;
import nars.core.NAR;
import nars.util.DefaultGraphizer;
import nars.util.NARGraph;
import org.jgrapht.Graph;

/**
 *
 */
public class NARGraphVis extends AnimatingGraphVis<Object,Object> implements EventObserver {
        
    
    final AtomicReference<Graph> displayedGraph = new AtomicReference();
    private final NAR nar;
    boolean showTaskLinks = false;
    boolean showTermLinks = true;
    float minPriority = 0;
    private boolean showBeliefs = false;    
    private boolean showQuestions = false;
    private boolean showTermContent = false;
    private final GraphDisplays displays;
    private NARGraphDisplay style;
    private GraphDisplay layout;
            
    public NARGraphVis(NAR n) {
        super(null, new GraphDisplays());
        this.nar = n;
        this.displays = (GraphDisplays)getDisplay();
        
        update(new NARGraphDisplay(), new FastOrganicLayout());
    }
    
    public void update(NARGraphDisplay style, GraphDisplay layout) {
        this.style = style;
        this.layout = layout;
        displays.sequence.clear();
        displays.sequence.add(style);
        displays.sequence.add(layout);
    }

    @Override
    public void onVisible(boolean showing) {  
        nar.memory.event.set(this, showing, FrameEnd.class, ResetEnd.class);        
    }

    @Override
    public void event(Class event, Object[] args) {
        if (event == FrameEnd.class) {
            displayedGraph.set(nextGraph());
        }
        else if (event == ResetEnd.class) {
            displayedGraph.set(null);
        }
    }
        
            
    protected Graph nextGraph() {
        if (nar == null) return null;
                
        return new NARGraph().add(nar, new NARGraph.ExcludeBelowPriority(minPriority), new DefaultGraphizer(showBeliefs, showBeliefs, showQuestions, showTermContent, 0, showTermLinks, showTaskLinks));
    }

    @Override
    public void setUpdateNext() {
        super.setUpdateNext();        
        
        Graph ng = nextGraph();
        if (ng!=null)
            displayedGraph.set(ng);
    }

    
    
    @Override
    public Graph<Object, Object> getGraph() {        
        if (displayedGraph == null)
            return null;
        return displayedGraph.get();
    }
    

    public void setTaskLinks(boolean taskLinks) {
        this.showTaskLinks = taskLinks;
    }

    
    
    public JPanel newLayoutPanel() {
        JPanel j = new JPanel(new FlowLayout(FlowLayout.LEFT));
        final JComboBox modeSelect = new JComboBox();
        modeSelect.addItem("Organic");
        modeSelect.addItem("GridSort");
        modeSelect.addItem("Circle Anim");
        modeSelect.addItem("Circle Fixed");       
        modeSelect.addItem("Grid");
        //modeSelect.setSelectedIndex(cg.mode);
        modeSelect.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                //cg.mode = modeSelect.getSelectedIndex();
                setUpdateNext();
            }
        });
        j.add(modeSelect);
        return j;
    }
    
    public JPanel newGraphPanel() {
        JPanel j = new JPanel(new FlowLayout(FlowLayout.LEFT));

        //final int numLevels = ((Bag<Concept>)n.memory.concepts).levels;
        NSlider maxLevels = new NSlider(1, 0, 1) {
            @Override
            public void onChange(float v) {
                minPriority = (float) (1.0 - v);
                setUpdateNext();
            }
        };
        maxLevels.setPrefix("Min Level: ");
        maxLevels.setPreferredSize(new Dimension(80, 25));
        j.add(maxLevels);        

        final JCheckBox termlinkEnable = new JCheckBox("TermLinks");
        termlinkEnable.setSelected(showTermLinks);
        termlinkEnable.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                showTermLinks = (termlinkEnable.isSelected());                
                setUpdateNext();
            }
        });
        j.add(termlinkEnable);        
        
        final JCheckBox taskLinkEnable = new JCheckBox("TaskLinks");
        taskLinkEnable.setSelected(showTaskLinks);
        taskLinkEnable.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                showTaskLinks = (taskLinkEnable.isSelected());                
                setUpdateNext();
            }
        });
        j.add(taskLinkEnable);
        
        final JCheckBox beliefsEnable = new JCheckBox("Beliefs");
        beliefsEnable.setSelected(showBeliefs);
        beliefsEnable.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                showBeliefs = (beliefsEnable.isSelected());
                setUpdateNext();
            }
        });
        j.add(beliefsEnable);
        
        return j;
    }
    public JPanel newStylePanel() {
        JPanel j = new JPanel(new FlowLayout(FlowLayout.LEFT));
        j.add(style.getControls());
        return j;
    }

    
    
    
    
}