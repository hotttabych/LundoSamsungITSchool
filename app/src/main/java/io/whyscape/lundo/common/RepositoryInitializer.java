package io.whyscape.lundo.common;

import android.content.Context;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;
import io.whyscape.lundo.data.repository.NoteRepositoryImpl;
import io.whyscape.lundo.data.repository.ProxyNoteRepository;
import io.whyscape.lundo.data.db.DiaryDatabase;

@Singleton
public class RepositoryInitializer {

    private final Context context;
    private final ProxyNoteRepository proxyRepo;

    private DiaryDatabase diaryDbInstance = null;

    @Inject
    public RepositoryInitializer(@ApplicationContext Context context, ProxyNoteRepository proxyRepo) {
        this.context = context;
        this.proxyRepo = proxyRepo;
    }

    public void initSecureDb(byte[] passphrase) {
        if (diaryDbInstance == null) {
            diaryDbInstance = DiaryDatabase.Companion.create(context, passphrase);
            NoteRepositoryImpl realRepo = new NoteRepositoryImpl(diaryDbInstance.noteDao());
            proxyRepo.setRealRepository(realRepo);
        }
    }

    public void clearSecureDb() {
        if (diaryDbInstance != null) {
            diaryDbInstance.close();
            diaryDbInstance = null;
        }
        proxyRepo.clearRealRepository();
    }
}