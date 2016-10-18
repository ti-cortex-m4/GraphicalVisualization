package com.org.practicum.tree;

import java.util.Comparator;

import com.org.practicum.neo4j.ElementBean;

// class to compare exposure amounts to assist in sorting elements based on the exposure amount
public class ExposureAmountComparator implements Comparator<ElementBean> {
	@Override
	public int compare(ElementBean o1, ElementBean o2) {
		if (o1 != null && o2 != null) {
			if (o1.getExposureAmount() == null && o2.getExposureAmount() == null)
				return 0;
			else if (o1.getExposureAmount() == null || o1.getExposureAmount().trim().length() <= 0)
				return -1;
			else if (o2.getExposureAmount() == null || o2.getExposureAmount().trim().length() <= 0)
				return 1;
			else
				return (int) (Long.parseLong(o1.getExposureAmount()) - Long.parseLong(o2.getExposureAmount()));
		} else
			return 0;
	}
}
