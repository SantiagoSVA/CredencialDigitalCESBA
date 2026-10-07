package mx.edu.cesba.credencial.photo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import androidx.exifinterface.media.ExifInterface;
import java.io.File;
import java.io.IOException;

/** Fotos privadas, sin permiso de almacenamiento; muestreo y corrección EXIF. */
public final class PhotoStore {
    private PhotoStore() {}
    public static File create(Context context) throws IOException {
        File dir = new File(context.getFilesDir(), "photos");
        if (!dir.exists() && !dir.mkdirs()) throw new IOException("No se pudo crear la carpeta de fotos.");
        return File.createTempFile("cesba_", ".jpg", dir);
    }
    public static boolean isReadable(Context context, String path) {
        if (path == null || path.isEmpty()) return false;
        try {
            File file = new File(path);
            File dir = new File(context.getFilesDir(), "photos").getCanonicalFile();
            return dir.equals(file.getCanonicalFile().getParentFile())
                    && file.isFile() && file.canRead() && file.length() > 0;
        } catch (IOException exception) { return false; }
    }
    public static Bitmap load(Context context, String path, int targetSize) throws IOException {
        if (!isReadable(context, path)) throw new IOException("Foto no disponible.");
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        if (options.outWidth <= 0 || options.outHeight <= 0) throw new IOException("Foto dañada.");
        options.inSampleSize = 1;
        while (Math.max(options.outWidth, options.outHeight) / options.inSampleSize > targetSize * 2)
            options.inSampleSize *= 2;
        options.inJustDecodeBounds = false;
        Bitmap original = BitmapFactory.decodeFile(path, options);
        if (original == null) throw new IOException("No se pudo decodificar la foto.");
        ExifInterface exif = new ExifInterface(path);
        Matrix transform = new Matrix();
        transform.postRotate(exif.getRotationDegrees());
        if (exif.isFlipped()) transform.postScale(-1f, 1f);
        Bitmap oriented = Bitmap.createBitmap(original, 0, 0, original.getWidth(), original.getHeight(), transform, true);
        if (oriented != original) original.recycle();
        return oriented;
    }
    public static void delete(Context context, String path) {
        if (isReadable(context, path)) new File(path).delete();
    }
}
