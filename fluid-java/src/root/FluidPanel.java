package root;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

public class FluidPanel extends JPanel implements ActionListener {
	//// SIMULATION PROPERTIES ------------------------------------------------
	
	// Action Commands
	public static final String TICK = "Tick";
	
	// Time
	private int _framerate = 60;
	private int _timerDelay = 1000 / _framerate;
	private double _timeStep = 0.002;
	private Timer _timer;
	
	// Options
	private boolean _running = false;
	private boolean _renderVelocity = false;
	private boolean _renderFlow = true;
	private boolean _drawDensity = false;
	
	// Fluid Dimensions
	private int _fluidPower = 8;
	private int _fluidSize = (1 << _fluidPower);
	private double _frameScale = FluidWindow.DEFAULT_SCALE;
	private int _frameSize = (int) (_fluidSize * _frameScale);
	
	// Rendering
	private BufferedImage _canvas;
	private BufferedImage _upscaled;
	private AffineTransform _imgScale;
	private AffineTransformOp _imgScaleOp; 
	
	// Fluid Field
	private FluidField _fluid;
	
	//// CONSTRUCTOR ----------------------------------------------------------
	
	public FluidPanel(){
		init();
	}
	
	//// INITIALIZATION -------------------------------------------------------
	
	public void init(){
		this.setScale(_frameScale);
		this.setFluidPower(_fluidPower);
		
		// Timer
		_timer = new Timer(_timerDelay, this);
		_timer.setActionCommand(TICK);
		_timer.start();
		
		// Add Listeners
		this.addMouseListener(new MouseAdapter(){
			public void mouseReleased(MouseEvent e) {
				update();
				//_fluid.project(_timeStep);
			}
		});
		
		// Mouse Motion Listener
		this.addMouseMotionListener(new MouseMotionAdapter(){
			int prevX = 0;
			int prevY = 0;
			
			public void mouseMoved(MouseEvent e){
				prevX = e.getX();
				prevY = e.getY();
			}
			
			public void mouseDragged(MouseEvent e) {
				int x = e.getX();
				int y = e.getY();
				float diffX = x - prevX;
				float diffY = y - prevY;
				prevX = x;
				prevY = y;

				_fluid.impulse(diffX*10, diffY*10, (int)((prevX%_frameSize)/_frameScale), (int)((prevY%_frameSize)/_frameScale), _drawDensity);
			}
		});
		
		// Key Listener
		this.addKeyListener(new KeyAdapter(){
			public void keyPressed(KeyEvent e){
				switch(e.getKeyCode()){
				case KeyEvent.VK_SPACE:
					_fluid.resetDensity();
					break;
				case KeyEvent.VK_R:
					_fluid.resetVelocity();
					break;
				case KeyEvent.VK_A:
					_fluid.randomVelocity();
					break;
				}
			}
		});

		// Begin Rendering
		_running = true;
	}
	
	//// RESET METHODS --------------------------------------------------------
	
	// Call when resolution or scaling changes; resets necessary BufferedImages
	public void resetResolution(){
		_frameSize = (int) (_fluidSize * _frameScale);
		
		// Refresh Images
		if(_upscaled != null) _upscaled.flush();
		_upscaled = new BufferedImage(_frameSize, _frameSize, BufferedImage.TYPE_INT_RGB);
		if(_canvas != null) _canvas.flush();
		_canvas = new BufferedImage(_fluidSize, _fluidSize, BufferedImage.TYPE_INT_RGB);
		
		// Image Scaling
		_imgScale = new AffineTransform();
		_imgScale.scale(_frameScale, _frameScale);
		_imgScaleOp = new AffineTransformOp(_imgScale, AffineTransformOp.TYPE_BILINEAR);
		
	}
	
	public void resetDensity(){
		_fluid.resetDensity();
	}
	
	public void resetVelocity(){
		_fluid.resetVelocity();
	}
	
	//// UPDATE ---------------------------------------------------------------
	
	public void update(){
		if(!_running) return;
		_fluid.update(_timeStep);
	}
	
	//// RENDERING ------------------------------------------------------------
	
	public void paint(Graphics gr){
		Graphics2D g = (Graphics2D)gr;

		_canvas.flush();
		if(_renderVelocity){
			_fluid.renderVelocity(_canvas);
		} else {
			_fluid.renderDensity(_canvas);
		}
		
		// Scale Canvas
		_imgScaleOp.filter(_canvas, _upscaled);
		if(_renderFlow){
			_fluid.renderFlow(_upscaled, _frameScale);
		}
		
		// Tile
		int tiles = this.getWidth() / _frameSize + 1;
		for(int i = 0; i < tiles; i++){
			for(int j = 0; j < tiles; j++){
				g.drawImage(_upscaled, null, _frameSize*i, _frameSize*j);
			}
		}
	}
	
	//// GETTER / SETTER METHODS ----------------------------------------------
	
	public void setFluidPower(int n){
		if(n > 4 && n < 10){
			// Dimensions
			_fluidPower = n;
			_fluidSize = (1 << _fluidPower);
			resetResolution();
			
			// Reset Fluid
			_fluid = new FluidField(_fluidSize);
		}
	}
	
	public void setScale(double scale){
		_frameScale = scale;
		resetResolution();
	}
	
	public void setViscosity(double v){
		_fluid.setViscosity(v);
	}
	
	public void toggleRunning() {
		_running = !_running;
	}
	
	public void toggleRenderMode(){
		_renderVelocity = !_renderVelocity;
	}
	
	public void toggleDrawDensity(){
		_drawDensity = !_drawDensity;
	}
	
	
	//// ACTIONLISTENER -------------------------------------------------------

	@Override
	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
		Object src = e.getSource();
		
		if(cmd.equals(TICK)){
			update();
			this.repaint();
		}
	}
}
