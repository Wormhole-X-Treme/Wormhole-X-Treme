package com.wormhole_xtreme.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;


import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.material.MaterialData;

import com.wormhole_xtreme.WormholeXTreme;
import com.wormhole_xtreme.config.ConfigManager;
import com.wormhole_xtreme.logic.StargateUpdateRunnable;
import com.wormhole_xtreme.logic.StargateUpdateRunnable.ActionToTake;
import com.wormhole_xtreme.utils.WorldUtils;


/** 
 * WormholeXtreme Stargate 
 * @author Ben Echols (Lologarithm) 
 */ 
public class Stargate 
{	
	// Used to parse
	public byte LoadedVersion = -1;
	public long GateId = -1;
	/** 
	 *  Name of this gate, used to index and target.
	 */
	public String Name = "";
	/** 
	 * Name of person who made the gate
	 */
	public String Owner = null;
	/** 
	 * Network gate is connected to.
	 */
	public StargateNetwork Network;
	/** 
	 * Is activated through sign destination?
	 */
	public boolean IsSignPowered;
	/**
	 * The gateshape that this gate uses.
	 * This affects woosh depth and later materials
	 */
	public StargateShape GateShape;
	
	public World MyWorld;
	// Iris passcode (IDC_
	public String IrisDeactivationCode = "";
	// Is Iris active?
	public boolean IrisActive = false;
	// Default Iris setting
	public boolean IrisDefaultActive = false;
	// Is this stargate already active? Can be active remotely and have no target of its own.
	public boolean Active = false;
	// Is this stargate already lit up? 
	public boolean LitGate = false;
	// Stargate that is the target of this gate.
	public Stargate Target = null;
	// temp int id of target starget
	public long temp_target_id = -1;
	
	// Location of the Button/Lever that activates this gate.
	public Block ActivationBlock;
	// Location of the Button/Lever that activates this gate.
	public Block IrisActivationBlock;
	// Block to place stargate name
	public Block NameBlockHolder;
	
	// Block to temp store the sign.
	public Block TeleportSignBlock;
	// Sign to choose teleport target from (optional)
	public Sign TeleportSign;
	// Block to teleport from;
	public Location TeleportLocation;
	
	// Direction that the stargate faces.
	public BlockFace Facing;
		
	// The current target on the sign, only used if IsSignPowered is true
	public Stargate SignTarget;
	// Temp target id to store when loading gates.
	public long temp_sign_target = -1;
	// Index in network the sign is pointing at
	public int SignIndex = 0;

	
	// List of all blocks contained in this stargate, including buttons and levers.
	public ArrayList<Location> Blocks = new ArrayList<Location>();
	// List of all blocks that turn to water on activation
	public ArrayList<Location> WaterBlocks = new ArrayList<Location>();
	// List of all blocks that turn on when gate is active
	public ArrayList<Location> LightBlocks = new ArrayList<Location>();
	
	// Used to track active scheduled tasks.
	private int ActivateTaskId;
	private int ShutdownTaskId;
	
	public Stargate()
	{
		
	}
	
	/*public Stargate(World w, String name, StargateNetwork network, byte[] gate_data)
	{
		this.Name = name;
		this.Network = network;
		ParseVersionedData(gate_data, w);
	}*/
	
	public void FillGateWater()
	{
		Material m = ConfigManager.getPortalMaterial();
		for( Location bc : this.WaterBlocks )
		{
			Block b = MyWorld.getBlockAt(bc.getBlockX(), bc.getBlockY(), bc.getBlockZ());
			b.setType(m);
		}
	}
	
	public void FillGateIris()
	{
		Material m = ConfigManager.getIrisMaterial();
		for( Location bc : this.WaterBlocks )
		{
			Block b = MyWorld.getBlockAt(bc.getBlockX(), bc.getBlockY(), bc.getBlockZ());
			b.setType(m);
		}
	}
	
	public void EmptyGateWater()
	{
		for( Location bc : this.WaterBlocks )
		{
			Block b = MyWorld.getBlockAt(bc.getBlockX(), bc.getBlockY(), bc.getBlockZ());
			b.setType(Material.AIR);
		}		
	}
	
	int animation_step = 0;
	ArrayList<Block> AnimatedBlocks = new ArrayList<Block>();
	public void AnimateOpening()
	{
		Material woosh_material = ConfigManager.getPortalMaterial();
		int woosh_depth = this.GateShape.woosh_depth;
		
		if ( animation_step == 0 && woosh_depth > 0)
		{
			
			for ( Location b : WaterBlocks )
			{
				Block r = MyWorld.getBlockAt(b.getBlockX(), b.getBlockY(), b.getBlockZ()).getRelative(Facing);
				if ( r.getType() != ConfigManager.getStargateMaterial() )
				{
					r.setType(woosh_material);
					AnimatedBlocks.add(r);
					StargateManager.opening_animation_blocks.put(r.getLocation(), r);
				}
			}
			
			animation_step++;
			WormholeXTreme.Scheduler.scheduleSyncDelayedTask(WormholeXTreme.ThisPlugin, new StargateUpdateRunnable(this, ActionToTake.ANIMATE_OPENING), 4);
		}
		else if ( animation_step < woosh_depth )
		{
			// start = animation_step * WaterBlocks.size();
			// count = waterblocks.size()
			int size = AnimatedBlocks.size();
			int start = WaterBlocks.size();
			for ( int i = (size - start); i < size; i++ )
			{
				Block b = AnimatedBlocks.get(i);
				Block r = b.getRelative(Facing);
				if ( r.getType() != ConfigManager.getStargateMaterial() )
				{
					r.setType(woosh_material);
					AnimatedBlocks.add(r);
					StargateManager.opening_animation_blocks.put(r.getLocation(), r);
				}
			}
			
			animation_step++;
			// Longer wait if we have reached the max depth
			if ( animation_step == woosh_depth )
				WormholeXTreme.Scheduler.scheduleSyncDelayedTask(WormholeXTreme.ThisPlugin, new StargateUpdateRunnable(this, ActionToTake.ANIMATE_OPENING), 8);
			else
				WormholeXTreme.Scheduler.scheduleSyncDelayedTask(WormholeXTreme.ThisPlugin, new StargateUpdateRunnable(this, ActionToTake.ANIMATE_OPENING), 4);
		}
		else if ( animation_step >= woosh_depth )
		{
			for ( int i = 0; i < WaterBlocks.size(); i++ )
			{
				int index = AnimatedBlocks.size() - 1;
				if ( index >= 0 )
				{
					Block b = AnimatedBlocks.get(index);
					b.setType(Material.AIR);
					AnimatedBlocks.remove(index);
					StargateManager.opening_animation_blocks.remove(b.getLocation());
				}
			}
			if ( animation_step < ((woosh_depth * 2) - 1 ) )
			{
				animation_step++;
				WormholeXTreme.Scheduler.scheduleSyncDelayedTask(WormholeXTreme.ThisPlugin, new StargateUpdateRunnable(this, ActionToTake.ANIMATE_OPENING), 3);
			}
			else
				animation_step = 0;
		}
	}

	public void ActivateStargate() 
	{
		this.Active = true;
	}
	
	public void LightStargate()
	{
		this.LitGate = true;
		// Light up blocks
		//this.ActivationBlock.getFace(WorldUtils.getInverseDirection(this.Facing)).setType(StargateActiveMaterial);

		if ( LightBlocks != null )
		{
			for ( Location l : LightBlocks)
			{
				Block b = MyWorld.getBlockAt(l.getBlockX(), l.getBlockY(), l.getBlockZ()); 
				b.setType(ConfigManager.getActivateMaterial());
			}
		}
	}
	
	public void StartActivationTimer(Player p)
	{
		if ( this.ActivateTaskId >= 0)
		{
			WormholeXTreme.Scheduler.cancelTask(this.ActivateTaskId);
		}
		
		int timeout = ConfigManager.getTimeoutActivate() * 20;
		this.ActivateTaskId = WormholeXTreme.Scheduler.scheduleSyncDelayedTask(WormholeXTreme.ThisPlugin, new StargateUpdateRunnable(this, p, ActionToTake.DEACTIVATE), timeout);
		WormholeXTreme.ThisPlugin.prettyLog(Level.FINE, false, "Wormhole \""+ this.Name + "\" ActivateTaskID \"" + this.ActivateTaskId + "\" created.");
	}

	public void StopActivationTimer(Player p)
	{
		if ( this.ActivateTaskId >= 0)
		{
		    WormholeXTreme.ThisPlugin.prettyLog(Level.FINE, false, "Wormhole \""+ this.Name + "\" ActivateTaskID \"" + this.ActivateTaskId + "\" cancelled.");
			WormholeXTreme.Scheduler.cancelTask(this.ActivateTaskId);
			this.ActivateTaskId = -1;
		}
	}

	public void TimeoutStargate(Player p)
	{
		if ( this.ActivateTaskId >= 0 )
		{
		    WormholeXTreme.ThisPlugin.prettyLog(Level.FINE, false, "Wormhole \""+ this.Name + "\" ActivateTaskID \"" + this.ActivateTaskId + "\" timed out.");
			this.ActivateTaskId = -1;
		}
		// Deactivate if player still hasn't picked a target.
		Stargate s = null;
		if ( p != null)
			s = StargateManager.RemoveActivatedStargate(p);
		else
			s = this;
		
		// Only send a message if the gate was still in the remotely activated gates list.
		if ( s != null)
		{
			// Make sure to reset iris if it should be on.
			if ( this.IrisDefaultActive ) 
			{
				SetIrisActive(IrisDefaultActive);
			}
			if ( this.LitGate )
			{
				s.UnLightStargate();
			}
			
			if ( p != null )
				p.sendMessage("Gate: " + this.Name + " timed out and deactivated.");
		}
	}
	
	public void DeActivateStargate()
	{
		this.Active = false;
	}
	
	public void UnLightStargate()
	{
		this.LitGate = false;
		
		// Remove Light Up Blocks
		if ( LightBlocks != null )
		{
			for ( Location l : LightBlocks)
			{
				Block b = MyWorld.getBlockAt(l.getBlockX(), l.getBlockY(), l.getBlockZ()); 
				b.setType(ConfigManager.getStargateMaterial());
			}
		}
		
		//this.ActivationBlock.getFace(WorldUtils.getInverseDirection(this.Facing)).setType(StargateMaterial);
	}
	
	/**
	 * This method activates the current stargate as if it had just been dialed.
	 * This includes filling the event horizon, canceling any other shutdown events,
	 * scheduling the shutdown time and scheduling the WOOSH if enabled.
	 * Failed task schedules will cause gate to not activate, fill, or animate.  
	 */
	public void DialStargate()
	{
		if ( this.ShutdownTaskId >= 0)
		{
			WormholeXTreme.Scheduler.cancelTask(this.ShutdownTaskId);
		}
		
		int timeout = ConfigManager.getTimeoutShutdown() * 20;
		if ( timeout > 0 )
		{
			this.ShutdownTaskId = WormholeXTreme.Scheduler.scheduleSyncDelayedTask(WormholeXTreme.ThisPlugin, new StargateUpdateRunnable(this, ActionToTake.SHUTDOWN), timeout);
			WormholeXTreme.ThisPlugin.prettyLog(Level.FINE, false, "Wormhole \"" + this.Name + "\" ShutdownTaskID \"" + this.ShutdownTaskId + "\" created." );
			if (this.ShutdownTaskId == -1 ) 
			{ 
				WormholeXTreme.ThisPlugin.prettyLog(Level.WARNING,false,"Failed to schdule wormhole shutdown timeout: " + timeout + " Received task id of -1. Attempting again.");
				this.ShutdownTaskId = WormholeXTreme.Scheduler.scheduleSyncDelayedTask(WormholeXTreme.ThisPlugin, new StargateUpdateRunnable(this, ActionToTake.SHUTDOWN), timeout);
				if (this.ShutdownTaskId == -1 ) 
				{
					ShutdownStargate();
					WormholeXTreme.ThisPlugin.prettyLog(Level.SEVERE,false,"Failed to schdule wormhole shutdown timeout: " + timeout + " Received task id of -1. Wormhole forced closed NOW.");
				}
			}
		}
		
		if ((this.ShutdownTaskId >= 0) || ( timeout == 0 ))
		{
			if ( !this.Active ) 
			{
				ActivateStargate();
			}
			if ( !this.LitGate)
			{
				LightStargate();
			}
			// Show water if you are dialing out OR if the iris isn't active
			if ( this.Target != null || !this.IrisActive )
			{
				FillGateWater();
				
				if (ConfigManager.getPortalWoosh())
				{
					WormholeXTreme.Scheduler.scheduleSyncDelayedTask(WormholeXTreme.ThisPlugin, new StargateUpdateRunnable(this, ActionToTake.ANIMATE_OPENING));
				}
			}
		}
		else 
		{
			WormholeXTreme.ThisPlugin.prettyLog(Level.WARNING, false, "No wormhole. No visual events.");
		}
	}
	
	/**
	 * This method takes in a remote stargate and dials it if it is not active.
	 * @param target
	 * @return True if successful, False if remote target is already Active or if there is a failure scheduling stargate shutdowns.
	 */
	public boolean DialStargate(Stargate target)
	{
		if ( this.ActivateTaskId >= 0 )
		{
			WormholeXTreme.Scheduler.cancelTask(ActivateTaskId);
		}
		
		if ( !target.LitGate )
		{
			WorldUtils.checkChunkLoad(target.ActivationBlock);
			this.Target = target;
			this.DialStargate();
			target.DialStargate();
			if ((this.Active) && (this.Target.Active)) 
			{
				return true;
			} 
			else if ((this.Active) && (!this.Target.Active))
			{
				this.ShutdownStargate();
				WormholeXTreme.ThisPlugin.prettyLog(Level.WARNING, false, "Far wormhole failed to open. Closing local wormhole for safety sake.");
			} 
			else if ((!this.Active) && (target.Active))
			{
				target.ShutdownStargate();
				WormholeXTreme.ThisPlugin.prettyLog(Level.WARNING,false, "Local wormhole failed to open. Closing far end wormhole for safety sake.");
			}
		}
		
		return false;
	}
	
	/**
	 * This method takes in a remote stargate and dials it if it is not active.
	 * @param target
	 * @return True if successful, False if remote target is already Active or if there is a failure scheduling stargate shutdowns.
	 */
	public boolean ForceDialStargate(Stargate target)
	{
		if ( this.ActivateTaskId >= 0 )
		{
			WormholeXTreme.Scheduler.cancelTask(ActivateTaskId);
		}
		
		//if ( !target.LitGate )
		//{
			WorldUtils.checkChunkLoad(target.ActivationBlock);
			this.Target = target;
			this.DialStargate();
			target.DialStargate();
			if ((this.Active) && (target.Active)) 
			{
				return true;
			} 
			else if ((this.Active) && (!target.Active))
			{
				this.ShutdownStargate();
				WormholeXTreme.ThisPlugin.prettyLog(Level.WARNING, false, "Far wormhole failed to open. Closing local wormhole for safety sake.");
			} 
			else if ((!this.Active) && (target.Active))
			{
				target.ShutdownStargate();
				WormholeXTreme.ThisPlugin.prettyLog(Level.WARNING,false, "Local wormhole failed to open. Closing far end wormhole for safety sake.");
			}
		//}
		
		return false;
	}
	
	public void ShutdownStargate()
	{
		if ( this.ShutdownTaskId >= 0 )
		{
		    WormholeXTreme.ThisPlugin.prettyLog(Level.FINE, false, "Wormhole \"" + this.Name + "\" ShutdownTaskID \"" + this.ShutdownTaskId + "\" cancelled.");
			WormholeXTreme.Scheduler.cancelTask(this.ShutdownTaskId);
			this.ShutdownTaskId = -1;
		}
		
		if ( this.Target != null )
		{
			this.Target.ShutdownStargate();
		}

		this.Target = null;
		this.DeActivateStargate();
		this.UnLightStargate();
		
		// Only set back to air if iris isn't on.
		// If the iris should be on, we will make it that way.
		if ( this.IrisDefaultActive )
		{
			SetIrisActive(IrisDefaultActive);
		}
		else if ( !this.IrisActive )
		{
			this.EmptyGateWater();
		}
	}
	
	public void CompleteGate(String name, String idc)
	{
		this.Name = name;

		// 1. Setup Name Sign
		if ( this.NameBlockHolder != null )
		{
		    this.SetupGateSign();
		}
		// 2. Set up Iris stuff
		SetIrisDeactivationCode(idc);
	}
	
	/*
	 * Deal with initial gate sign placement, orientation and text for name, network, and owner.
	 */
	public void SetupGateSign()
	{
	      
	        Block name_sign = this.NameBlockHolder.getFace(Facing);
			name_sign.setType(Material.WALL_SIGN);		
			switch ( Facing )
			{
			    case NORTH:
			        name_sign.setData((byte)0x04);
			        break;
			    case SOUTH:
			        name_sign.setData((byte)0x05);
			        break;
			    case EAST:
			        name_sign.setData((byte)0x02);
			        break;
			    case WEST:
			        name_sign.setData((byte)0x03);
			        break;
			}
			name_sign.getState().setData(new MaterialData(Material.WALL_SIGN));		
			Sign sign = (Sign)name_sign.getState();
			sign.setLine(0, "-" + this.Name + "-");
	
			if ( this.Network != null )
			{
				sign.setLine(1, "N:" + this.Network.netName);
			}
			
			if ( this.Owner != null )
			{
				sign.setLine(2, "O:" + this.Owner);
			}
	        sign.update();
	}
	
	/*
	 * Setup Levers for IRIS control when IDC is set.
	 */
	public void SetupIrisLever(boolean add)
	{
		if ( add )
		{
	    	Block iris_block = this.ActivationBlock.getFace(BlockFace.DOWN);
	    	this.IrisActivationBlock = iris_block;
			this.Blocks.add(IrisActivationBlock.getLocation());
			
			this.IrisActivationBlock.setType(Material.LEVER);
			switch (Facing)
			{
			    case SOUTH:
			        this.IrisActivationBlock.setData((byte)0x01);
			        break;
			    case NORTH:
			        this.IrisActivationBlock.setData((byte)0x02);
			        break;
			    case WEST:
			        this.IrisActivationBlock.setData((byte)0x03);
			        break;
			    case EAST:
			        this.IrisActivationBlock.setData((byte)0x04);
			        break;   
			}
		}
		else
		{
			Blocks.remove(this.IrisActivationBlock.getLocation());
			this.IrisActivationBlock.setType(Material.AIR);
		}
		
	}
	public void SetIrisDeactivationCode ( String idc )
	{
		this.IrisDeactivationCode = idc;
		this.SetIrisActive(false);
		
		// If empty string make sure to make lever area air instead of lever.
		if ( !idc.equals("") )
		{
		    this.SetupIrisLever(true);
		}
		else
		{
			this.SetupIrisLever(false);
			this.SetIrisActive(false);
		}
	}
	
	/**
	 * This method should only be called when the Iris lever is hit.
	 * This toggles the current state of the Iris and then sets that state to be the default.
	 */
	public void ToggleIrisLever()
	{
		ToggleIrisActive();
		IrisDefaultActive = IrisActive;
	}
	
	/**
	 * This method toggles the current state of the iris.
	 */
	public void ToggleIrisActive()
	{
		IrisActive = !IrisActive;
		
		if ( IrisActive )
		{
			FillGateIris();
		}
		else
		{
			if ( Active )
			{
				FillGateWater();
			}
			else
			{
				EmptyGateWater();
			}
		}		
	}
	
	/**
	 * This method toggles the current state of the iris.
	 */
	public void SetIrisActive(boolean active)
	{
		IrisActive = active;
		
		if ( IrisActive )
		{
			FillGateIris();
		}
		else
		{
			EmptyGateWater();
		}		
	}
	
	// version_byte|ActivationBlock|IrisActivationBlock|NameBlockHolder|TeleportLocation|IsSignPowered|TeleportSign|
	//  facing_len|facing_string|idc_len|idc|IrisActive|num_blocks|Blocks|num_water_blocks|WaterBlocks
	


	public boolean TryClickTeleportSign(Block clicked) 
	{
		if ( TeleportSign == null && TeleportSignBlock != null )
		{
			if ( TeleportSignBlock.getType() == Material.WALL_SIGN )
			{
				this.SignIndex = -1;
				TeleportSign = (Sign)TeleportSignBlock.getState();
				TeleportSignClicked();
			}
		}
		else if ( WorldUtils.isSameBlock(clicked, TeleportSignBlock) )
		{
			TeleportSignClicked();
			return true;
		}
		
		return false;
	}
	
	private HashMap<Integer, Stargate> gate_order = new HashMap<Integer,Stargate>();
	public void TeleportSignClicked()
	{
		if ( this.SignIndex == -1 )
		{
			this.TeleportSign.setLine(0, "-" + this.Name + "-");
			this.SignIndex++;
		}
		
		synchronized ( Network.gateLock )
		{
			if ( this.Network.gate_list.size() == 0 || this.Network.gate_list.size() == 1)
			{
				this.TeleportSign.setLine(1, "");
				this.TeleportSign.setLine(2, "No Other Gates");
				this.TeleportSign.setLine(3, "");
				this.SignTarget = null;
				return;
			}

			if ( SignIndex >= this.Network.gate_list.size() )
				SignIndex = 0;
			
			if ( this.Network.gate_list.get(SignIndex).Name.equals(this.Name) )
			{
				SignIndex++;
				if ( SignIndex == this.Network.gate_list.size() )
					SignIndex = 0;
			}
			
			if ( this.Network.gate_list.size() == 2 )
			{
				gate_order.clear();
				gate_order.put(Integer.valueOf(2), this.Network.gate_list.get(SignIndex));

					
				this.TeleportSign.setLine(1, "");
				this.TeleportSign.setLine(2, ">" + gate_order.get(Integer.valueOf(2)).Name + "<");
				this.TeleportSign.setLine(3, "");
				this.SignTarget = this.Network.gate_list.get(SignIndex);
			}
			else if ( this.Network.gate_list.size() == 3 )
			{
				gate_order.clear();
				int order_index = 1;
				//SignIndex++;
				while ( gate_order.size() < 2)
				{
					if ( SignIndex >= this.Network.gate_list.size() )
						SignIndex = 0;
					
					if ( this.Network.gate_list.get(SignIndex).Name.equals(this.Name) )
					{
						SignIndex++;
						if ( SignIndex == this.Network.gate_list.size() )
							SignIndex = 0;
					}
					

					gate_order.put(Integer.valueOf(order_index), this.Network.gate_list.get(SignIndex));
					order_index++;
					if ( order_index == 4)
						order_index = 1;
					SignIndex++;
				}
				
				this.TeleportSign.setLine(1, gate_order.get(Integer.valueOf(1)).Name);
				this.TeleportSign.setLine(2, ">" + gate_order.get(Integer.valueOf(2)).Name + "<");
				this.TeleportSign.setLine(3, "");
				
				this.SignTarget = gate_order.get(Integer.valueOf(2));
				this.SignIndex = Network.gate_list.indexOf(gate_order.get(Integer.valueOf(2)));
			}		
			else
			{
				gate_order.clear();
				int order_index = 1;
				while ( gate_order.size() < 3)
				{
					if ( SignIndex == this.Network.gate_list.size() )
						SignIndex = 0;
					
					if ( this.Network.gate_list.get(SignIndex).Name.equals(this.Name) )
					{
						SignIndex++;
						if ( SignIndex == this.Network.gate_list.size() )
							SignIndex = 0;
					}
					
					gate_order.put(Integer.valueOf(order_index), this.Network.gate_list.get(SignIndex));
					order_index++;

					SignIndex++;
				}
				
				this.TeleportSign.setLine(1, gate_order.get(Integer.valueOf(3)).Name);
				this.TeleportSign.setLine(2, ">" + gate_order.get(Integer.valueOf(2)).Name + "<");
				this.TeleportSign.setLine(3, gate_order.get(Integer.valueOf(1)).Name);
				
				this.SignTarget = gate_order.get(Integer.valueOf(2));
				this.SignIndex = Network.gate_list.indexOf(gate_order.get(Integer.valueOf(2)));
			}
		}
		
		this.TeleportSign.setData(this.TeleportSign.getData());		
		this.TeleportSign.update(true);
	}
	/*
	 * Delete Stargate Blocks
	 */
	public void DeleteGateBlocks()
	{
		for( Location bc : this.Blocks )
		{
			Block b = MyWorld.getBlockAt(bc.getBlockX(), bc.getBlockY(), bc.getBlockZ());
			b.setType(Material.AIR);
		}
	}

	/*
	 * Delete Stargate Portal Blocks
	 */
	public void DeletePortalBlocks()
	{
		for( Location bc : this.WaterBlocks )
		{
			Block b = MyWorld.getBlockAt(bc.getBlockX(), bc.getBlockY(), bc.getBlockZ());
			b.setType(Material.AIR);
		}
	}
	
	/*
	 * Delete Name Block
	 */
	public void DeleteNameBlock()
	{
		if (this.NameBlockHolder != null)
		{
			this.NameBlockHolder.setType(Material.AIR);
		}
	}
	
	/*
	 * Delete Teleport Sign Block
	 */
	public void DeleteTeleportSignBlock()
	{
		if (this.TeleportSignBlock != null)
		{
			this.TeleportSignBlock.setType(Material.AIR);
		}
	}
	
	/*
	 * Delete Name Sign
	 */
	public void DeleteNameSign()
	{
		if ( this.NameBlockHolder != null)
		{
			Block name_sign;
			if (( name_sign = this.NameBlockHolder.getFace(Facing)) != null)
			{
				name_sign.setType(Material.AIR);
			}
		}
	}
	
	/*
	 * Delete Teleport Sign
	 */
	public void DeleteTeleportSign()
	{
		if (this.TeleportSignBlock != null && this.TeleportSign != null)
		{
			Block teleport_sign = this.TeleportSignBlock.getFace(Facing);
			teleport_sign.setType(Material.AIR);
		}
	}
	/*
	 * Wipe Teleport Sign
	 */
	public void ResetTeleportSign()
	{
		if ( this.TeleportSignBlock != null && this.TeleportSign != null)
		{
			this.TeleportSign.setLine(0,  this.Name );
			this.TeleportSign.setLine(1, "");
			this.TeleportSign.setLine(2, "");
			this.TeleportSign.setLine(3, "");
		}
		
	}
	
	/*
	 * Delete Iris Lever
	 */
	public void DeleteIrisLever()
	{
        if (this.IrisActivationBlock != null)
        {
            this.IrisActivationBlock.setType(Material.AIR);
        }
	}
}