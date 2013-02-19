package com.ForgeEssentials.data;

import java.util.ArrayList;
import java.util.Map.Entry;

import net.minecraftforge.common.Configuration;

import com.ForgeEssentials.api.data.DataStorageManager;
import com.ForgeEssentials.api.data.EnumDriverType;
import com.ForgeEssentials.api.data.IDataDriver;
import com.ForgeEssentials.api.data.ITypeInfo;
import com.ForgeEssentials.api.data.TypeData;
import com.ForgeEssentials.api.data.ITypeInfo;
import com.ForgeEssentials.api.data.TypeMultiValInfo;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

public abstract class AbstractDataDriver implements IDataDriver
{
	private HashMultimap<String, String> classRegister = HashMultimap.create();
	private boolean hasLoaded;
	
	@Override
	public void onClassRegistered(ITypeInfo tagger)
	{
	}

	@Override
	public final String getName()
	{
		return this.getClass().getSimpleName().replace(AbstractDataDriver.class.getSimpleName(), "");
	}

	@Override
	public boolean saveObject(Object o)
	{
		boolean flag = false;
		
		if (!classRegister.containsEntry(getName(), o.getClass().getName()))
		{
			this.onClassRegistered(DataStorageManager.getInfoForType(o.getClass()));
			classRegister.put(getName(), o.getClass().getName());
		}

		ITypeInfo t;
		if ((t = DataStorageManager.getInfoForType(o.getClass())) != null)
		{
			flag = true;
			saveData(o.getClass(), t.getTypeDataFromObject(o));
		}

		return flag;
	}

	@Override
	public Object loadObject(Class type, String loadingKey)
	{
		Object newObject = null;
		TypeData data = loadData(type, loadingKey);
		ITypeInfo info = DataStorageManager.getInfoForType(type);

		if (data != null && data.getAllFields().size() > 0)
		{
			newObject = createFromFields(data, info);
		}

		return newObject;
	}

	@Override
	public Object[] loadAllObjects(Class type)
	{
		ArrayList<Object> list = new ArrayList<Object>();
		TypeData[] objectData = loadAll(type);
		ITypeInfo info = DataStorageManager.getInfoForType(type);

		// Each element of the field array represents an object, stored as an
		// array of fields.
		Object tmp;
		if (objectData != null && objectData.length > 0)
		{
			for (TypeData data : objectData)
			{
				tmp = createFromFields(data, info);
				list.add(tmp);
			}
		}

		return list.toArray(new Object[list.size()]);
	}

	@Override
	public boolean deleteObject(Class type, String loadingKey)
	{
		return deleteData(type, loadingKey);
	}

	private Object createFromFields(TypeData data, ITypeInfo info)
	{
		Object val;
		// loops through all fields of this class.
		for (Entry<String, Object> entry : data.getAllFields())
		{
			// if it needs reconstructing before this class...
			if (entry.getValue() instanceof TypeData)
			{
				// reconstruct the class...
				val = createFromFields((TypeData) entry.getValue(), info.getInfoForField(entry.getKey()));

				// re-add it to the map.
				data.putField(entry.getKey(), val);
			}
		}

		// actually reconstruct this class
		val = info.reconstruct(data);

		// return the reconstructed value.
		return val;
	}
	
	@Override
	public void parseConfigs(Configuration config, String category, String worldName) throws Exception
	{
		loadFromConfigs(config, category, worldName);
		hasLoaded = true;
	}
	
	@Override
	public boolean hasLoaded()
	{
		return hasLoaded;
	}

	abstract public void loadFromConfigs(Configuration config, String category, String worldName) throws Exception;

	abstract protected boolean saveData(Class type, TypeData fieldList);

	abstract protected TypeData loadData(Class type, String uniqueKey);

	abstract protected TypeData[] loadAll(Class type);

	abstract protected boolean deleteData(Class type, String uniqueObjectKey);
}
