class TestServe {
    public static void main(String[] args) throws Exception {
        
        UDPServer sv = new UDPServer(9876);
        
        sv.serve();

    }
}