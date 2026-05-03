package com.cam.scanner.scantopdf.android.util;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Came from GIFEncoder initially.
 * Modified - to allow for output compressed data without the block counts
 * which breakup the compressed data stream for GIF.
 *
 * @since 5.0.2
 */
public class BitFile {
    OutputStream output_;
    byte buffer_[];
    int index_;
    int bitsLeft_;    // bits left at current index that are avail.

    /**
     * note this also indicates gif format BITFile.
     **/
    boolean blocks_ = false;

    /**
     * @param output destination for output data
     * @param blocks GIF LZW requires block counts for output data
     **/
    public BitFile(OutputStream output, boolean blocks) {
        output_ = output;
        blocks_ = blocks;
        buffer_ = new byte[256];
        index_ = 0;
        bitsLeft_ = 8;
    }

    public void flush() throws IOException {
        int numBytes = index_ + (bitsLeft_ == 8 ? 0 : 1);
        if (numBytes > 0) {
            if (blocks_)
                output_.write(numBytes);
            output_.write(buffer_, 0, numBytes);
            buffer_[0] = 0;
            index_ = 0;
            bitsLeft_ = 8;
        }
    }

    public void writeBits(int bits, int numbits) throws IOException {
        int bitsWritten = 0;
        int numBytes = 255;        // gif block count
        do {
            // This handles the GIF block count stuff
            if ((index_ == 254 && bitsLeft_ == 0) || index_ > 254) {
                if (blocks_)
                    output_.write(numBytes);

                output_.write(buffer_, 0, numBytes);

                buffer_[0] = 0;
                index_ = 0;
                bitsLeft_ = 8;
            }

            if (numbits <= bitsLeft_) // bits contents fit in current index byte
            {
                if (blocks_) // GIF
                {
                    buffer_[index_] |= (bits & ((1 << numbits) - 1)) << (8 - bitsLeft_);
                    bitsWritten += numbits;
                    bitsLeft_ -= numbits;
                    numbits = 0;
                } else {
                    buffer_[index_] |= (bits & ((1 << numbits) - 1)) << (bitsLeft_ - numbits);
                    bitsWritten += numbits;
                    bitsLeft_ -= numbits;
                    numbits = 0;

                }
            } else    // bits overflow from current byte to next.
            {
                if (blocks_)    // GIF
                {
                    // if bits  > space left in current byte then the lowest order bits
                    // of code are taken and put in current byte and rest put in next.
                    buffer_[index_] |= (bits & ((1 << bitsLeft_) - 1)) << (8 - bitsLeft_);
                    bitsWritten += bitsLeft_;
                    bits >>= bitsLeft_;
                    numbits -= bitsLeft_;
                    buffer_[++index_] = 0;
                    bitsLeft_ = 8;
                } else {
                    // if bits  > space left in current byte then the highest order bits
                    // of code are taken and put in current byte and rest put in next.
                    // at highest order bit location !!
                    int topbits = (bits >>> (numbits - bitsLeft_)) & ((1 << bitsLeft_) - 1);
                    buffer_[index_] |= topbits;
                    numbits -= bitsLeft_;    // ok this many bits gone off the top
                    bitsWritten += bitsLeft_;
                    buffer_[++index_] = 0;    // next index
                    bitsLeft_ = 8;
                }
            }

        } while (numbits != 0);
    }
}

