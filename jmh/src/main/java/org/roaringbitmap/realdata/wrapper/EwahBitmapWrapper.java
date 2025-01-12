package org.roaringbitmap.realdata.wrapper;

import com.googlecode.javaewah.EWAHCompressedBitmap;
import com.googlecode.javaewah.FastAggregation;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Iterator;

final class EwahBitmapWrapper implements Bitmap {

   private final EWAHCompressedBitmap bitmap;

   EwahBitmapWrapper(EWAHCompressedBitmap bitmap) {
      this.bitmap = bitmap;
   }

   @Override
   public boolean contains(int i) {
      return bitmap.get(i);
   }

   @Override
   public int last() {
      return bitmap.reverseIntIterator().next();
   }

   @Override
   public int cardinality() {
      return bitmap.cardinality();
   }

   @Override
   public BitmapIterator iterator() {
      return new EwahIteratorWrapper(bitmap.intIterator());
   }

   @Override
   public BitmapIterator reverseIterator() {
      return new EwahIteratorWrapper(bitmap.reverseIntIterator());
   }

   @Override
   public Bitmap and(Bitmap other) {
      return new EwahBitmapWrapper(bitmap.and(((EwahBitmapWrapper) other).bitmap));
   }

   @Override
   public Bitmap or(Bitmap other) {
      return new EwahBitmapWrapper(bitmap.or(((EwahBitmapWrapper) other).bitmap));
   }

   @Override
   public Bitmap xor(Bitmap other) {
      return new EwahBitmapWrapper(bitmap.xor(((EwahBitmapWrapper) other).bitmap));
   }

   @Override
   public Bitmap andNot(Bitmap other) {
      return new EwahBitmapWrapper(bitmap.andNot(((EwahBitmapWrapper) other).bitmap));
   }

   @Override
   public BitmapAggregator naiveAndAggregator() {
      return new BitmapAggregator() {
         @Override
         public Bitmap aggregate(Iterable<Bitmap> bitmaps) {
            final Iterator<Bitmap> i = bitmaps.iterator();
            EWAHCompressedBitmap bitmap = ((EwahBitmapWrapper) i.next()).bitmap;
            while(i.hasNext()) {
               bitmap = bitmap.and(((EwahBitmapWrapper) i.next()).bitmap);
            }
            return new EwahBitmapWrapper(bitmap);
         }
      };
   }

   @Override
   public BitmapAggregator naiveOrAggregator() {
      return new BitmapAggregator() {
         @Override
         public Bitmap aggregate(Iterable<Bitmap> bitmaps) {
             final Iterator<Bitmap> i = bitmaps.iterator();
             EWAHCompressedBitmap bitmap = ((EwahBitmapWrapper) i.next()).bitmap;
             while(i.hasNext()) {
                 bitmap = bitmap.or(((EwahBitmapWrapper) i.next()).bitmap);
             }
             return new EwahBitmapWrapper(bitmap);
         }
      };
   }

   @Override
   public BitmapAggregator priorityQueueOrAggregator() {
      return new BitmapAggregator() {
         @Override
         public Bitmap aggregate(final Iterable<Bitmap> bitmaps) {
            Iterator<EWAHCompressedBitmap> iterator = new Iterator<EWAHCompressedBitmap>() {
               final Iterator<Bitmap> i = bitmaps.iterator();

               @Override
               public boolean hasNext() {
                  return i.hasNext();
               }

               @Override
               public EWAHCompressedBitmap next() {
                  return ((EwahBitmapWrapper) i.next()).bitmap;
               }
            };
            return new EwahBitmapWrapper(FastAggregation.or(iterator));
         }
      };
   }

   @Override
   public void serialize(DataOutputStream dos) throws IOException {
      bitmap.serialize(dos);
   }

}
