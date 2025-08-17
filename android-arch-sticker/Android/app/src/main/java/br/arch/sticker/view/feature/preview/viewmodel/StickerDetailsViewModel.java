package br.arch.sticker.view.feature.preview.viewmodel;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.core.util.Pair;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import br.arch.sticker.core.pattern.CallbackResult;
import br.arch.sticker.domain.service.delete.DeleteStickerAssetService;
import br.arch.sticker.domain.service.delete.DeleteStickerService;

public class StickerDetailsViewModel extends AndroidViewModel {
    private final DeleteStickerAssetService deleteStickerAssetService;
    private final DeleteStickerService deleteStickerService;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private final MutableLiveData<Pair<Boolean, String>> deletedSticker = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessageLiveData = new MutableLiveData<>();

    public StickerDetailsViewModel(@NonNull Application application) {
        super(application);
        Context context = getApplication().getApplicationContext();
        this.deleteStickerService = new DeleteStickerService(context);
        this.deleteStickerAssetService = new DeleteStickerAssetService(context);
    }

    public void startDeleted(String stickerPackIdentifier, String fileName) {
        executor.submit(() -> {
            CallbackResult<Boolean> resultDataBase = deleteStickerService.deleteStickerByPack(stickerPackIdentifier,
                    fileName);
            if (resultDataBase.isFailure()) {
                errorMessageLiveData.postValue(resultDataBase.getError().getMessage());
                return;
            }

            if (resultDataBase.isWarning()) {
                errorMessageLiveData.postValue(resultDataBase.getWarningMessage());
                return;
            }

            CallbackResult<Boolean> resultAsset = deleteStickerAssetService.deleteStickerAsset(stickerPackIdentifier,
                    fileName);
            if (resultAsset.isFailure()) {
                errorMessageLiveData.postValue(resultAsset.getError().getMessage());
                return;
            } else if (resultAsset.isWarning()) {
                errorMessageLiveData.postValue(resultAsset.getWarningMessage());
            }

            deletedSticker.postValue(
                    new Pair<>((resultDataBase.getData() && resultAsset.getData()), stickerPackIdentifier));
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executor.shutdownNow();
    }
}
