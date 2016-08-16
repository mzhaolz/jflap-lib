/*
*  JFLAP - Formal Languages and Automata Package
*
*
*  Susan H. Rodger
*  Computer Science Department
*  Duke University
*  August 27, 2009

*  Copyright (c) 2002-2009
*  All rights reserved.

*  JFLAP is open source software. Please see the LICENSE for terms.
*
*/

package edu.duke.cs.jflap.file;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * This is a registry of the codec, both {@link Encoder} and {@link Decoder}
 * objects. It helps in the selection of a codec for structures, and imposes an
 * order of what codecs should be tried.
 *
 * @author Thomas Finley
 */
public class CodecRegistry {
	/** The encoders of the registry. */
	private final List<Encoder> encoders = new ArrayList<>();

	/** The decoders of the registry. */
	private final List<Decoder> decoders = new ArrayList<>();

	/**
	 * Adds a codec to the registry.
	 *
	 * @param codec
	 *            the codec to add
	 */
	public void add(final Codec codec) {
		addEncoder(codec);
		addDecoder(codec);
	}

	/**
	 * Add a {@link Decoder} to the registry.
	 *
	 * @param decoder
	 *            the decoder to add
	 */
	private void addDecoder(final Decoder decoder) {
		decoders.add(decoder);
	}

	/**
	 * Add a {@link Encoder} to the registry.
	 *
	 * @param encoder
	 *            the encoder to add
	 */
	private void addEncoder(final Encoder encoder) {
		encoders.add(encoder);
	}

	/**
	 * Returns a list of decoders. All decoders are returned. The decoders are
	 * returned in the order they were placed in the registry with the
	 * {@link #add} method.
	 *
	 * @return the immutable list of decoders
	 */
	public List<Decoder> getDecoders() {
		return Collections.unmodifiableList(decoders);
	}

	/**
	 * Returns a list of encoders that could encode a structure. The encoders
	 * are returned in the order they were placed in the registry with the
	 * {@link #add} method.
	 *
	 * @param structure
	 *            the structure the encoders should be able to possibly encode,
	 *            or <CODE>null</CODE> if all encoders should be returned
	 * @return the immutable list of encoders
	 */
	public List<Encoder> getEncoders(final Serializable structure) {
		if (structure == null) {
			return Collections.unmodifiableList(encoders);
		}
		final List<Encoder> validEncoders = new ArrayList<>();
		final Iterator<Encoder> it = encoders.iterator();
		while (it.hasNext()) {
			final Codec enc = (Codec) it.next();
			if (enc.canEncode(structure)) {
				validEncoders.add(enc);
			}
		}
		return Collections.unmodifiableList(validEncoders);
	}
}
