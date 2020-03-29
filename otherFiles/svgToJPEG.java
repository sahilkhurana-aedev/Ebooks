	private static void createJpegImage(String svgData) {
		String base64String = null;
		try {
			TranscoderInput input_svg_image = new TranscoderInput(new ByteArrayInputStream(svgData.getBytes()));
			ByteArrayOutputStream ostream = new ByteArrayOutputStream();
			TranscoderOutput output_image = new TranscoderOutput(ostream);

			JPEGTranscoder my_converter = new JPEGTranscoder();
			my_converter.addTranscodingHint(JPEGTranscoder.KEY_QUALITY, new Float(1.0));
			my_converter.transcode(input_svg_image, output_image);

			ostream.flush();
			ostream.close();
			byte[] imageBytes = ostream.toByteArray();
			printImageQuality(imageBytes, "D:/images/test1.jpeg");
		} catch (Throwable t) {

		}
	}

	private static void printImageQuality(byte[] frontSideByte, String filename) {
		try {
			// byte[] frontSideByte = Base64.decodeBase64(frontSideData);
			ByteArrayInputStream bis = new ByteArrayInputStream(frontSideByte);
			BufferedImage frontImage = ImageIO.read(bis);
			ImageIO.write(frontImage, "jpeg", new File(filename));

		} catch (Exception eeep) {
			System.out.println("exception...");
			eeep.printStackTrace();
		}
	}