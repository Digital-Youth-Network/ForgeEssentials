package com.forgeessentials.jscripting.wrapper.util;

import com.forgeessentials.jscripting.wrapper.JsWrapper;

import net.minecraft.util.AxisAlignedBB;

public class JsAxisAlignedBB extends JsWrapper<AxisAlignedBB>
{

    public JsAxisAlignedBB(AxisAlignedBB that)
    {
        super(that);
    }

    public JsAxisAlignedBB(double minX, double minY, double minZ, double maxX, double maxY, double maxZ)
    {
        this(AxisAlignedBB.fromBounds(minX, minY, minZ, maxX, maxY, maxZ));
    }

    public JsAxisAlignedBB setBounds(double minX, double minY, double minZ, double maxX, double maxY, double maxZ)
    {
        that = AxisAlignedBB.fromBounds(minX, minY, minZ, maxX, maxY, maxZ);
        return this;
    }

}