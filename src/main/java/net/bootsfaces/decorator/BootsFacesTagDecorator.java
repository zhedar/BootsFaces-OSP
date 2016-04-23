/**
 *  (C) 2013-2015 Stephan Rauh http://www.beyondjava.net
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.bootsfaces.decorator;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.faces.view.facelets.Tag;
import javax.faces.view.facelets.TagAttribute;
import javax.faces.view.facelets.TagAttributes;
import javax.faces.view.facelets.TagDecorator;

import net.bootsfaces.component.ComponentsEnum;

/**
 * This is one of the most important classes of AngularFaces. It converts
 * attributes to pass-through parameters, adds them to the list of JSF bean to
 * be synchronized with the client and implements a couple of pseudo JSF tags.
 */
public class BootsFacesTagDecorator implements TagDecorator {
	private static boolean active = false;

	private static final String HTML_NAMESPACE = "http://www.w3.org/1999/xhtml";
	private static final Logger LOGGER = Logger.getLogger(BootsFacesTagDecorator.class.getName());
	private static final String JSF_NAMESPACE = "http://xmlns.jcp.org/jsf/html";
	private static final String JSF_CORE_NAMESPACE = "http://java.sun.com/jsf/core";
	private static final String PASS_THROUGH_NAMESPACE = "http://xmlns.jcp.org/jsf/passthrough";
	private static final String BOOTSFACES_NAMESPACE = "http://bootsfaces.net/ui";
	private static final Map<String, String> bootsfacesTags;

	static {
		ComponentsEnum[] components = ComponentsEnum.values();
		bootsfacesTags = new HashMap<String, String>();
		for (ComponentsEnum component:components) {
			bootsfacesTags.put(component.tagname(), component.tagname());
		}
		System.out.println(bootsfacesTags.size());
	}

	public static boolean isActive() {
		return active;
	}

	private final RelaxedTagDecorator relaxedDecorator = new RelaxedTagDecorator();

	private Tag convertElementToInputText(Tag tag, TagAttributes modifiedAttributes) {
		TagAttribute[] attributes = modifiedAttributes.getAll();
		TagAttribute[] lessAttributes = Arrays.copyOf(attributes, attributes.length - 1);
		TagAttributes less = new AFTagAttributes(lessAttributes);
		Tag t = new Tag(tag.getLocation(), JSF_NAMESPACE, "inputText", "h:inputText", less);
		return t;
	}

	private Tag convertToInputText(Tag tag, TagAttributes attributeList) {
		TagAttribute[] attributes = attributeList.getAll();
		TagAttributes more = new AFTagAttributes(attributes);
		Tag t = new Tag(tag.getLocation(), JSF_NAMESPACE, "inputText", "inputText", more);
		return t;
	}

	private Tag convertDivElementToPanelGroup(Tag tag, TagAttributes modifiedAttributes, boolean isDiv) {
		TagAttribute[] attributes = modifiedAttributes.getAll();
		TagAttributes more = addBlockAttributeIfNeeded(tag, isDiv, attributes);
		Tag t = new Tag(tag.getLocation(), JSF_NAMESPACE, "panelGroup", "h:panelGroup", more);
		return t;
	}

	private Tag convertDivTagToPanelGroup(Tag tag, TagAttributes attributeList, boolean isDiv) {
		TagAttribute[] attributes = attributeList.getAll();
		TagAttributes more = addBlockAttributeIfNeeded(tag, isDiv, attributes);
		Tag t = new Tag(tag.getLocation(), JSF_NAMESPACE, "panelGroup", "panelGroup", more);
		return t;
	}

	private TagAttributes addBlockAttributeIfNeeded(Tag tag, boolean isDiv, TagAttribute[] attributes) {
		TagAttribute[] moreAttributes = attributes;
		if (isDiv) {
			moreAttributes = new TagAttribute[attributes.length + 1];
			for (int i = 0; i < attributes.length; i++)
				moreAttributes[i] = attributes[i];
			moreAttributes[attributes.length] = TagAttributeUtilities.createTagAttribute(tag.getLocation(),
					HTML_NAMESPACE, "display", "display", "block");
		}
		TagAttributes more = new AFTagAttributes(moreAttributes);
		return more;
	}

	/**
	 * Converts &lt;option&gt;firstComboboxItem&lt;/option&gt; to
	 * &lt;f:selectItem itemValue="firstComboxItem"&gt;.
	 */
	private Tag convertTofSelectItemText(Tag tag, TagAttributes attributeList) {
		TagAttribute[] attributes = attributeList.getAll();
		AFTagAttributes more = new AFTagAttributes(attributes);
		Tag t = new Tag(tag.getLocation(), JSF_CORE_NAMESPACE, "selectItem", "selectItem", more);
		return t;
	}

	@Override
	public Tag decorate(Tag tag) {
		Tag newTag = createTags(tag);
		return newTag;
	}

	private Tag createTags(Tag tag) {
		String ns = tag.getNamespace();
		// we only handle html tags!
		if (HTML_NAMESPACE.equals(ns)) {
			active = true;
			TagAttributes modifiedAttributes = tag.getAttributes();
			// Apache MyFaces converts HTML tag with jsf: namespace, but missing
			// an
			// attribute, into jsf:element tag. We'll fix this
			// for the special case of input fields.

			if ("element".equals(tag.getLocalName())) {
				TagAttribute tagAttribute = modifiedAttributes.get(PASS_THROUGH_NAMESPACE, "elementName");
				if ("input".equals(tagAttribute.getValue())) {
					return convertElementToInputText(tag, modifiedAttributes);
				}
				if ("div".equals(tagAttribute.getValue())) {
					return convertDivElementToPanelGroup(tag, modifiedAttributes, true);
				}
			}

			Tag newTag = relaxedDecorator.decorate(tag);
			if (newTag != null && newTag != tag) {
				return newTag;
			}

			if ("input".equals(tag.getLocalName())) {
				return convertToInputText(tag, modifiedAttributes);
			}

			if ("div".equals(tag.getLocalName())) {
				return convertDivTagToPanelGroup(tag, modifiedAttributes, true);
			}

			if ("option".equals(tag.getLocalName())) {
				return convertTofSelectItemText(tag, modifiedAttributes);
			}

			tag = convertBootsFacesTag(tag);
		}
		return tag;
	}

	private Tag convertBootsFacesTag(Tag tag) {
		if (HTML_NAMESPACE.equals(tag.getNamespace())) {
			String tagname = tag.getLocalName();
			if (bootsfacesTags.containsKey(tagname)) {
				AFTagAttributes modifiedAttributes = new AFTagAttributes(tag.getAttributes().getAll());
				Tag t = new Tag(tag.getLocation(), BOOTSFACES_NAMESPACE, tag.getLocalName(), tag.getQName(),
						modifiedAttributes);
				return t;
			}
		}
		return tag;

	}
}
