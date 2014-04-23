package root;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class FluidWindow extends JFrame implements ActionListener {
	//// VARIABLES ////////////////////////////////////////////////////////////
	
	// Default Parameters
	public static final double DEFAULT_VISCOSITY = 0.2;
	public static final double DEFAULT_SCALE = 2.0;
	
	// Actions
	public static final String TOGGLE_FLUID 	= "ToggleFluid";
	public static final String TOGGLE_RENDER_MODE= "ToggleRenderMode";
	public static final String TOGGLE_DRAW_DENSITY= "ToggleDrawDensity";
	public static final String RESET_DENSITY 	= "ResetDensity";
	public static final String RESET_VELOCITY 	= "ResetVelocity";
	public static final String SET_SIZE			= "SetSize";
	
	// Dimensions
	public static final int FRAME_SIZE = 1 << 9;
	
	// Interface Components
	public FluidPanel _fluidPanel;
	public JPanel _controlPanel;
		// Controls
		public JButton _playbackButton;
		public JButton _toggleRenderModeButton;
		public JButton _toggleDrawDensityButton;
		public JButton _resetDensityButton;
		public JButton _resetVelocityButton;
		public JLabel _sizeLabel;
		public JComboBox<String> _sizeCombo;
		public JSpinner _viscositySpinner;
		public JSpinner _scaleSpinner;
	
	//// CONSTRUCTOR //////////////////////////////////////////////////////////
	
	public static void main(String[] args){
		FluidWindow fw = new FluidWindow();
	}
	
	public FluidWindow(){
		init();
	}
	
	//// INITIALIZATION ///////////////////////////////////////////////////////
	
	public void init(){
		buildInterface();
	}
	
	public void buildInterface(){
		// Window Properties
		this.setTitle("Incompressible Fluid Simulation");
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		
		// Layout
		this.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = c.gridy = 0;
		c.gridheight = c.gridwidth = 1;
		c.weightx = c.weighty = 1.0;
		c.insets = new Insets(10, 10, 10, 10);
		
		// Prepare Fluid
		_fluidPanel = new FluidPanel();
		_fluidPanel.setPreferredSize(new Dimension(FRAME_SIZE, FRAME_SIZE));
		this.add(_fluidPanel, c);
		
		// Control Panel
		_controlPanel = new JPanel();
		_controlPanel.setPreferredSize(new Dimension(200, FRAME_SIZE));
		buildControlPanel();
		c.gridx++;
		c.insets.left = 0;
		this.add(_controlPanel, c);
		
		// Control Panel Commands
		_playbackButton.setActionCommand(TOGGLE_FLUID);
		_resetDensityButton.setActionCommand(RESET_DENSITY);
		_resetVelocityButton.setActionCommand(RESET_VELOCITY);
		_toggleRenderModeButton.setActionCommand(TOGGLE_RENDER_MODE);
		_toggleDrawDensityButton.setActionCommand(TOGGLE_DRAW_DENSITY);
		_viscositySpinner.addChangeListener(new ChangeListener(){
			public void stateChanged(ChangeEvent e) {
				_fluidPanel.setViscosity((double)_viscositySpinner.getValue()); 
			}
		});
		_scaleSpinner.addChangeListener(new ChangeListener(){
			public void stateChanged(ChangeEvent e) {
				_fluidPanel.setScale((double)_scaleSpinner.getValue()); 
			}
		});

		// Control Panel Listeners
		_playbackButton.addActionListener(this);
		_toggleRenderModeButton.addActionListener(this);
		_toggleDrawDensityButton.addActionListener(this);
		_resetDensityButton.addActionListener(this);
		_resetVelocityButton.addActionListener(this);
		
		// Set Visible
		this.pack();
		this.setVisible(true);
		this.setResizable(false);
	}
	
	public void buildControlPanel(){
		JLabel tempLabel;
		
		// Layout
		_controlPanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = c.gridy = 0;
		c.gridwidth = c.gridheight = 1;
		c.weighty = 0;
		c.weightx = 1.0;
		c.insets = new Insets(10,10,10,10);
		c.fill = GridBagConstraints.BOTH;
		c.anchor = GridBagConstraints.NORTH;
		
		// Misc
		_controlPanel.setBorder(BorderFactory.createTitledBorder("Controls"));
		
		// Size Combo Box
		tempLabel = new JLabel("Resolution");
		c.gridx = 1;
		_controlPanel.add(tempLabel,c);
		
		String[] comboLabels = new String[3];
		for(int i = 0; i < comboLabels.length; i++){
			int size = 1 << (i+6);
			comboLabels[i] = size + "x" + size;
		}
		_sizeCombo = new JComboBox<String>(comboLabels);
		_sizeCombo.setSelectedIndex(2);
		_sizeCombo.addActionListener(this);
		_sizeCombo.setActionCommand(SET_SIZE);
		c.gridx = 2;
		_controlPanel.add(_sizeCombo,c);
		
		// Viscosity Spinner
		tempLabel = new JLabel("Viscosity");
		tempLabel.setAlignmentY(CENTER_ALIGNMENT);
		c.gridx = 1;
		c.gridy++;
		c.insets.top = 0;
		_controlPanel.add(tempLabel,c);
		
		_viscositySpinner = new JSpinner(new SpinnerNumberModel(DEFAULT_VISCOSITY, 0f, 100f, 0.1));
		c.gridx = 2;
		_controlPanel.add(_viscositySpinner,c);
		
		// Viscosity Spinner
		tempLabel = new JLabel("Scaling");
		tempLabel.setAlignmentY(CENTER_ALIGNMENT);
		c.gridx = 1;
		c.gridy++;
		c.insets.top = 0;
		_controlPanel.add(tempLabel,c);
		
		_scaleSpinner = new JSpinner(new SpinnerNumberModel(DEFAULT_SCALE, 0.5, 4, 0.5));
		c.gridx = 2;
		_controlPanel.add(_scaleSpinner,c);
		
		//// BUTTONS ----------------------------------------------------------
		
		// Playback Button
		_playbackButton = new JButton("Pause");
		c.gridx = 1;
		c.gridy++;
		c.gridwidth = 2;
		_controlPanel.add(_playbackButton,c);

		// Toggle Render Mode
		_toggleRenderModeButton = new JButton("Toggle Render Mode");
		c.gridy++;
		_controlPanel.add(_toggleRenderModeButton,c);
		
		// Toggle Draw Density
		_toggleDrawDensityButton = new JButton("Toggle Inject Density");
		c.gridy++;
		_controlPanel.add(_toggleDrawDensityButton,c);
		
		// Reset Density
		_resetDensityButton = new JButton("Reset Density");
		c.gridy++;
		_controlPanel.add(_resetDensityButton,c);

		// Reset Velocity
		_resetVelocityButton = new JButton("Reset Velocity");
		c.gridy++;
		_controlPanel.add(_resetVelocityButton,c);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
		if(cmd.equals(FluidWindow.TOGGLE_FLUID)){
			_fluidPanel.toggleRunning();
		} else if(cmd.equals(TOGGLE_RENDER_MODE)){
			_fluidPanel.toggleRenderMode();
		} else if(cmd.equals(TOGGLE_DRAW_DENSITY)){
			_fluidPanel.toggleDrawDensity();
		} else if(cmd.equals(RESET_DENSITY)){
			_fluidPanel.resetDensity();
		} else if(cmd.equals(RESET_VELOCITY)){
			_fluidPanel.resetVelocity();
		} else if(cmd.equals(SET_SIZE)){
			_fluidPanel.setFluidPower(_sizeCombo.getSelectedIndex() + 6);
		}
	}
	
	//// ACTIONLISTENER ///////////////////////////////////////////////////////
	
}
