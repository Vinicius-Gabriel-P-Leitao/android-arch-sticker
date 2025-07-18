/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package br.arch.sticker.view.core.usecase.definition;

// @formatter:off
public enum MimeTypesSupported {
    IMAGE(new String[]{"image/jpeg", "image/jpg","image/png"}),
    ANIMATED(new String[]{"video/mp4", "image/gif"});

    private final String[] mimeTypes;

    MimeTypesSupported(String[] mimeTypes) {
        this.mimeTypes = mimeTypes;
    }

    public String[] getMimeTypes() {
        return mimeTypes;
    }

    public static MimeTypesSupported fromMimeType(String mimeType) throws IllegalArgumentException {
        for (MimeTypesSupported mimeTypesSupported : values()) {
            for (String supported : mimeTypesSupported.getMimeTypes()) {
                if (supported.equalsIgnoreCase(mimeType)) {
                    return mimeTypesSupported;
                }
            }
        }

        // TODO: Fazer exception personalizada.
        throw new IllegalArgumentException("MimeType não suportado: " + mimeType);
    }
}
