/*
 * Copyright (c) 2025 Vinícius Gabriel Pereira Leitão
 * All rights reserved.
 *
 * This source code is licensed under the Vinícius Non-Commercial Public License (VNCL),
 * which is based on the GNU General Public License v3.0, with additional restrictions regarding commercial use.
 */

package br.arch.sticker.core.error.throwable.base;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import br.arch.sticker.core.error.ErrorCodeProvider;

// @formatter:off
public class AppCoreStateException extends IllegalStateException {
    private final ErrorCodeProvider errorCode;
    private final Object[] details;

    public AppCoreStateException(@NonNull String message, @Nullable ErrorCodeProvider errorCode) {
        this(message, null, errorCode, null);
    }

    public AppCoreStateException(@NonNull String message, @Nullable Throwable cause, @Nullable ErrorCodeProvider errorCode) {
        this(message, cause, errorCode, null);
    }

    public AppCoreStateException(@NonNull String message, @Nullable Throwable cause, @Nullable ErrorCodeProvider errorCode, @Nullable Object[] details) {
        super(message, cause);
        this.errorCode = errorCode;
        this.details = details;
    }

    public ErrorCodeProvider getErrorCode() {
        return errorCode;
    }

    @Nullable
    public Object[] getDetails() {
        return details;
    }

    @Nullable
    public String getErrorCodeName() {
        if (errorCode instanceof Enum<?>) {
            return ((Enum<?>) errorCode).name();
        }

        return null;
    }
}

