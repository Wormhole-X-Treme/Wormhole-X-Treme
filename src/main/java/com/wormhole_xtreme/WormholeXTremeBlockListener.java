package com.wormhole_xtreme; 
 

import java.util.logging.Level;

import org.bukkit.block.*; 
import org.bukkit.Material; 
import org.bukkit.entity.Player; 

import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockInteractEvent;
import org.bukkit.event.block.BlockListener; 
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockRightClickEvent;

import com.wormhole_xtreme.config.ConfigManager;
import com.wormhole_xtreme.config.ConfigManager.StringTypes;
import com.wormhole_xtreme.logic.StargateHelper;
import com.wormhole_xtreme.model.Stargate;
import com.wormhole_xtreme.model.StargateManager;
import com.wormhole_xtreme.model.StargateShape;
import com.wormhole_xtreme.permissions.PermissionsManager;
import com.wormhole_xtreme.permissions.PermissionsManager.PermissionLevel;
import com.wormhole_xtreme.utils.WorldUtils;


 
/** 
 * WormholeXTreme Block Listener
 * @author Ben Echols (Lologarithm) 
 */ 
public class WormholeXTremeBlockListener extends BlockListener
{
	//private final Stargates plugin;
	
	public WormholeXTremeBlockListener(final WormholeXTreme plugin)
	{
		//this.plugin = plugin;
	}
	
	@Override
    public void onBlockRightClick(BlockRightClickEvent event)
	{
		Block clicked = event.getBlock();
		Player p = event.getPlayer();
		//System.out.println("Right Clicked.");
		if ( clicked.getType() == Material.STONE_BUTTON || clicked.getType() == Material.LEVER )
		{
			//this.ButtonLeverHit(event.getPlayer(), event.getBlock(), event.getDirection());
		}
		else if ( clicked.getType() == Material.WALL_SIGN )
		{
			Stargate s = StargateManager.getGateFromBlock(clicked);
			
			if ( s != null )
			{
				Boolean allowed = false;
				if ( WormholeXTreme.Permissions != null )
				{
					if ( WormholeXTreme.Permissions.has(p, "wormhole.use.sign"))
						allowed = true;
				}
				else 
				{
					PermissionLevel lvl = PermissionsManager.getPermissionLevel(p, s);
					if ( ( lvl == PermissionLevel.WORMHOLE_CREATE_PERMISSION || lvl == PermissionLevel.WORMHOLE_USE_PERMISSION || lvl == PermissionLevel.WORMHOLE_FULL_PERMISSION ) )
					{
							allowed = true;
					}
				}

				
				if ( p.isOp() || allowed) 
				{
					if ( s.TryClickTeleportSign(clicked) )
					{
						String target = "";
						if ( s.SignTarget != null )
						{
							target = s.SignTarget.Name;
						}
						p.sendMessage("Dialer set to: " + target);
					}
				}
				else 
				{
					p.sendMessage(ConfigManager.output_strings.get(StringTypes.PERMISSION_NO));
				}
			}
		}
			
	}
	
	@Override
    public void onBlockFlow(BlockFromToEvent event)
	{
		if ( StargateManager.isBlockInGate(event.getBlock()) )
		{
			event.setCancelled(true);
		}
	}

	@Override
	public void onBlockBreak(BlockBreakEvent e)
	{
	    Stargate s = StargateManager.getGateFromBlock(e.getBlock());
        Player p = e.getPlayer();
        if ( s != null )
        {
            boolean allowed = false;
            if ( WormholeXTreme.Permissions != null )
            {
                if ( WormholeXTreme.Permissions.has(p, "wormhole.remove.all"))
                {
                    allowed = true;
                }
                else if ( s.Owner != null)  
                {
                    if (s.Owner.equals(p.getName()) && WormholeXTreme.Permissions.has(p, "wormhole.remove.own"))
                    {
                        allowed = true;
                    }
                }
            }
            else 
            {
                PermissionLevel lvl = PermissionsManager.getPermissionLevel(p, s);
                if ( lvl == PermissionLevel.WORMHOLE_FULL_PERMISSION )
                {
                    allowed = true;
                }
            }

            if ( p.isOp() || allowed )
            {
                if ( !WorldUtils.isSameBlock(s.ActivationBlock, e.getBlock()) )
                {
                    if ( s.TeleportSignBlock != null && WorldUtils.isSameBlock(s.TeleportSignBlock, e.getBlock()) )
                    {
                        p.sendMessage("Destroyed DHD Sign. You will be unable to change dialing target from this gate.");
                        p.sendMessage("You can rebuild it later.");
                        s.TeleportSign = null;
                    } 
                    else if (e.getBlock().getType().equals(ConfigManager.getIrisMaterial()))
                    {
                        e.setCancelled(true);
                    } 
                    else
                    {
                        if (s.Active) 
                        {
                            s.DeActivateStargate();
                            s.EmptyGateWater();
                        }
                        if (s.LitGate) 
                        {
                            s.UnLightStargate();
                            s.StopActivationTimer(p);
                            StargateManager.RemoveActivatedStargate(p);
                        }
                        s.ResetTeleportSign();
                        s.DeleteNameSign();
                        if (!s.IrisDeactivationCode.equals(""))
                        {
                            s.DeleteIrisLever();
                        }
                        StargateManager.RemoveStargate(s);
                        p.sendMessage("Stargate Destroyed: " + s.Name);
                    }
                }
                else
                {
                    p.sendMessage("Destroyed DHD. You will be unable to dial out from this gate.");
                    p.sendMessage("You can rebuild it later.");
                }
                
            }
            else
            {
                e.setCancelled(true);
            }
        } 
	}
	
	@Override
    public void onBlockInteract(BlockInteractEvent event)
	{
		if ( event.isPlayer() )
		{
			Block clicked = event.getBlock();
			if ( clicked.getType() == Material.STONE_BUTTON || clicked.getType() == Material.LEVER )
			{
				if ( this.ButtonLeverHit((Player)event.getEntity(), event.getBlock(), null) )
				{
					event.setCancelled(true);
				}
			}
		}
		
	}

	@Override
    public void onBlockPhysics(BlockPhysicsEvent event)
	{
		if ( StargateManager.isBlockInGate(event.getBlock())) 
		{
			Material m = event.getBlock().getType();
			if ( m.equals(ConfigManager.getPortalMaterial()))
			{
				event.setCancelled(true);
			}
		}
	}
	
	private boolean ButtonLeverHit(Player p, Block clicked, BlockFace direction)
	{
		Stargate s = StargateManager.getGateFromBlock(clicked);
		
		if ( s != null  )
		{
			PermissionLevel lvl = PermissionsManager.getPermissionLevel(p, s);

			boolean allowed = false;
			if ( WormholeXTreme.Permissions != null )
			{
				if ( WormholeXTreme.Permissions.has(p, "wormhole.use.sign") || WormholeXTreme.Permissions.has(p, "wormhole.use.dialer") )
				{
					allowed = true;
				}
			}
			else if ( ( lvl == PermissionLevel.WORMHOLE_CREATE_PERMISSION || lvl == PermissionLevel.WORMHOLE_USE_PERMISSION || lvl == PermissionLevel.WORMHOLE_FULL_PERMISSION ) )
			{
				allowed = true;
			}

			if ( p.isOp() || allowed )
			{
				if ( WorldUtils.isSameBlock(s.ActivationBlock, clicked) )
				{
					this.HandleGateActivationSwitch(s, p);
				}
				else if ( WorldUtils.isSameBlock(s.IrisActivationBlock, clicked) )
				{
					this.HandleIrisActivationSwitch(s,p);
					if ((s.Active) && (!s.IrisActive)) 
					{
						s.FillGateWater();
					}
				}
			}
			else
			{
				p.sendMessage(ConfigManager.output_strings.get(StringTypes.PERMISSION_NO));
			}
			
			return true;
		}
		else 
		{
			if ( direction == null )
			{
				switch ( clicked.getData() )
				{
				case 1:
					direction = BlockFace.SOUTH;
					break;
				case 2:
					direction = BlockFace.NORTH;
					break;
				case 3:
					direction = BlockFace.WEST;
					break;
				case 4:
					direction = BlockFace.EAST;
					break;
				}
				
				if ( direction == null)
				{
					return false;
				}
			}
			// Check to see if player has already run the "build" command.
			StargateShape shape = StargateManager.GetPlayerBuilderShape(p);
			
			Stargate new_gate = null;
			if ( shape != null )
			{
				new_gate = StargateHelper.checkStargate(clicked, direction, shape);
			}
			else
			{
				new_gate = StargateHelper.checkStargate(clicked, direction);
			}
			
			if ( new_gate != null )
			{
				boolean allowed = false;
				if ( WormholeXTreme.Permissions != null )
				{
					if ( WormholeXTreme.Permissions.has(p, "wormhole.build"))
					{
						allowed = true;
					}
				}
				else 
				{
					PermissionLevel lvl = PermissionsManager.getPermissionLevel(p, new_gate);
					if ( ( lvl == PermissionLevel.WORMHOLE_CREATE_PERMISSION || lvl == PermissionLevel.WORMHOLE_FULL_PERMISSION ) )
					{
						allowed = true;
					}
				}

				if ( p.isOp() || allowed )
				{
					if ( new_gate.IsSignPowered )
					{
						p.sendMessage("Stargate Design Valid with Sign Nav.");
						if ( new_gate.Name.equals("") )
						{
							p.sendMessage("Stargate name invalid. Replace sign and try again.");
							p.sendMessage(ConfigManager.output_strings.get(StringTypes.CONSTRUCT_NAME_INVALID));
						}
						else
						{
							boolean success = StargateManager.CompleteStargate(p, new_gate);
							if ( success )
								p.sendMessage(ConfigManager.output_strings.get(StringTypes.CONSTRUCT_SUCCESS));
							else
							{
								p.sendMessage("Stargate constrution failed!?");
							}
						}
						
					}
					else
					{
						// Print to player that it was successful!
						p.sendMessage("Stargate Design Valid.");
						p.sendMessage("To complete type: /wormhole complete <name> idc=[IDC] net=[NET]");
						p.sendMessage("IDC and NET are optional.");
						// Add gate to unnamed gates.
						StargateManager.AddIncompleteStargate(p, new_gate);
					}
				}
				else
				{
					if ( new_gate.IsSignPowered )
					{
						new_gate.Network.gate_list.remove(new_gate);
						new_gate.TeleportSign.setLine(0, new_gate.Name);
					}
					StargateManager.RemoveIncompleteStargate(p);
					p.sendMessage(ConfigManager.output_strings.get(StringTypes.PERMISSION_NO));
				}	
				return true;
			}
			else
			{
				WormholeXTreme.ThisPlugin.prettyLog(Level.FINEST, false, p.getName() + " has pressed a button or level but did not find any properly created gates.");
			}
		}
		
		return false;
	}

	private void HandleIrisActivationSwitch(Stargate s, Player p) 
	{
		s.ToggleIrisLever();
	}

	private void HandleGateActivationSwitch(Stargate s, Player p) 
	{
		if ( s.Active || s.LitGate )
		{
			if ( s.Target != null)
			{
				//Shutdown stargate
				s.ShutdownStargate();
				p.sendMessage(ConfigManager.output_strings.get(StringTypes.GATE_SHUTDOWN));
			}
			else
			{
				Stargate s2 = StargateManager.RemoveActivatedStargate(p);
				if ( s2 != null && s.GateId == s2.GateId )
				{
					s.StopActivationTimer(p);
					s.DeActivateStargate();
					s.UnLightStargate();
					p.sendMessage(ConfigManager.output_strings.get(StringTypes.GATE_DEACTIVATED));
				}
				else
				{
					p.sendMessage(ConfigManager.output_strings.get(StringTypes.GATE_REMOTE_ACTIVE));
				}
			}
				
		}
		else
		{
			if ( s.IsSignPowered  )
			{
				boolean allowed = false;
				if ( WormholeXTreme.Permissions != null )
				{
					if ( WormholeXTreme.Permissions.has(p, "wormhole.use.sign") )
					{
						allowed = true;
					}
				}
				else
				{
					allowed = true;
				}
				
				if ( p.isOp() || allowed )
				{
					if ( s.TeleportSign == null && s.TeleportSignBlock != null )
					{
						s.TryClickTeleportSign(s.TeleportSignBlock);
					}
					
					if ( s.SignTarget != null)
					{
						if ( s.DialStargate(s.SignTarget) )
						{
							p.sendMessage("Stargates connected!");
						}
						else
						{
							p.sendMessage(ConfigManager.output_strings.get(StringTypes.GATE_REMOTE_ACTIVE));
						}
					}
					else
					{
						p.sendMessage(ConfigManager.output_strings.get(StringTypes.TARGET_INVALID));
					}
				}
				else
				{
					p.sendMessage(ConfigManager.output_strings.get(StringTypes.PERMISSION_NO));
				}
			}
			else
			{
				//Activate Stargate
				p.sendMessage(ConfigManager.output_strings.get(StringTypes.GATE_ACTIVATED));
				p.sendMessage("To dial type: /dial <gatename>");
				StargateManager.AddActivatedStargate(p, s);
				s.StartActivationTimer(p);
				s.LightStargate();
			}
		}
	}
} 