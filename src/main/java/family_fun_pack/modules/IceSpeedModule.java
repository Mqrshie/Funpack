package family_fun_pack.modules;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import family_fun_pack.FamilyFunPack;
import family_fun_pack.network.PacketListener;
import net.minecraft.init.Blocks;

public class IceSpeedModule extends Module{

    public IceSpeedModule() {
        super("IceSpeed", "Ice go brrrr");
    }

    @Override
    public void enable() {
        Blocks.ICE.slipperiness = Blocks.PACKED_ICE.slipperiness = Blocks.FROSTED_ICE.slipperiness = 1.075f;
    }

    @Override
    public void disable() {
        Blocks.ICE.slipperiness = Blocks.PACKED_ICE.slipperiness = Blocks.FROSTED_ICE.slipperiness = 0.97f;
    }

}
